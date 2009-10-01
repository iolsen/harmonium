package com.tivo.hme.sim;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.IHmeProtocol;

// we need to use the swing timer, instead of java.util.Timer, because
// java.util.Timer calls back outside of Swing's event dispatching thread.
import javax.swing.Timer;

/**
 * SimKeyManager manages key event delivery for the HME simulator.
 *
 * Unfortunately, java does not support the notion of repeat key
 * events. Instead, it sends multiple PRESSED events for the same repeating key,
 * at a repeat interval defined at the OS level. Further, some operating systems
 * send RELEASED events with every PRESSED event. So, on some operating systems,
 * the signature of a repeat key is a single PRESSED event; on other operating
 * systems, the signature is a RELEASED and PRESSED key sequence.
 *
 * This class is an OS independent abstraction that translates java key events
 * into their corresponding HME key events.
 *
 * When a key is pressed, held, and released on some operating systems, we see
 * the following sequence of key events:
 *
 * PRESSED    // key is pressed
 * PRESSED    // key repeat 1
 * PRESSED    // key repeat 2
 * PRESSED    // key repeat 3
 * RELEASED   // key is released
 *
 * on other operating systems, we see the following for the same action:
 *
 * PRESSED    // key is pressed
 * RELEASED   // key repeat 1
 * PRESSED    // key repeat 1
 * RELEASED   // key repeat 2
 * PRESSED    // key repeat 2
 * RELEASED   // key repeat 3
 * PRESSED    // key repeat 3
 * RELEASED   // key is released
 *
 * In the above, the timestamp on the RELEASED and PRESSED events for a repeat
 * is nearly identical since the operating system is generating a fake RELEASED
 * event to go with each repeating PRESSED event.
 *
 * To work around the inconsistencies between the two examples above, we keep
 * track of the last pressed key, and we start a timer whenever a RELEASED event
 * comes in. The timer has a very small timeout - 30ms. If a PRESSED event for
 * the same key comes in before the timer for the RELEASED event fires, we
 * assume that the PRESSED and RELEASED events really represent a repeating key
 * event. If no PRESSED key is received within the 30ms and the RELEASED timer
 * fires, we assume that the RELEASED event was a real RELEASED key. This
 * introduces a negligible delay in delivering RELEASED events.
 *
 * There is the possibility that the OS will not deliver the corresponding
 * PRESSED event within the 30ms window, which will cause a RELEASED event,
 * instead of a PRESSED event. In most cases this should not happen, but when it
 * does, the consequence is just a RELEASED-PRESSED event series, instead of a
 * REPEAT event. We should try to come up with a mechanism that fixes this, but
 * for now...
 */
class SimKeyManager implements ActionListener, FocusListener
{
    /**
     * The raw code of the last key that was "pressed" by the user. We track
     * this to determine which key type to send with REPEAT and RELEASE actions.
     */
    private int lastPressedKeyRawCode;

    /**
     * Pointer back to our SimPane so we can determine which SimResource to
     * target our events to.
     */
    private SimPane pane;

    /**
     * Our timers.
     */
    private Timer keyRepeatTimer;
    private Timer keyReleaseTimer;

    //
    // Timer values, in milliseconds. The repeat times are taken from the native
    // HME receiver.
    //
    private static final int repeatTimerInitial = 1000;
    private static final int repeatTimerRate = 150;

    //
    // 30ms is pretty arbitrary. We shouldn't go much lower, because we start
    // getting close to the operating system's tick resolution.
    //
    // Note that the release timer is a non-repeating timer.
    //
    private static final int releaseTimerRate = 30;


    SimKeyManager( SimPane source )
    {
        this.pane = source;

        //
        // we listen to the SimPane for focus changes so we know to stop
        // generating repeat key events.
        //
        this.pane.addFocusListener( this );
    }

    /**
     * The SimPane calls into us here whenever it gets a swing KeyEvent.
     */
    public void processKeyEvent( KeyEvent e )
    {
        if ( this.getHmeKeyCodeForRawCode( e.getKeyCode() ) == IHmeProtocol.KEY_UNKNOWN )
        {
            // not a key we care about
            return;
        }

        int action;

        switch (e.getID())
        {
          case KeyEvent.KEY_PRESSED:
          {
              action = IHmeProtocol.KEY_PRESS;
              break;
          }

          case KeyEvent.KEY_RELEASED:
          {
              action = IHmeProtocol.KEY_RELEASE;
              break;
          }

          default:
          {
              // we don't care about other types of events
              return;
          }
        }

        // If the if test at the top returned a code other than KEY_UNKNOWN and
        // the action is PRESSED or RELEASED, we do care about the key, so we
        // consume it.
        e.consume();
        this.processKeyEvent( action, e.getKeyCode() );
    }

    private void processKeyEvent( int action, int rawcode )
    {
        switch( action )
        {
          case IHmeProtocol.KEY_PRESS:
          {
              //
              // if a key press comes in before the release timer fires, it
              // means that there was a release followed by an immediate press
              // of the same key (signature of a repeat on some operating
              // systems), or the user is a really fast typist.
              //
              // For the former, on some operating systems, this is the
              // signature of a key repeat. so, we just want to disable the
              // release timer to make sure we don't end up sending a release
              // event, and let the repeat timer do its work.
              //
              // For the latter, we still want to disable the release timer,
              // since we'll generate a release event for the
              // lastPressedKeyRawCode in the code below.
              //
              this.stopKeyReleaseTimer();

              if ( lastPressedKeyRawCode == rawcode )
              {
                  // this is just a repeat key - let our repeat timer handle it.
                  return;
              }

              int lastPressedKeyCode = this.getHmeKeyCodeForRawCode( lastPressedKeyRawCode );
              if ( lastPressedKeyCode != IHmeProtocol.KEY_UNKNOWN )
              {
                  //
                  // A new key was "pressed" before we sent a "released" event
                  // for the last pressed key. Send a released event for the old
                  // key now.
                  //

                  //
                  // The lastPressedKeyRawCode is set to 0 when we call
                  // stopKeyRepeatTimer() below, so we want to be sure and
                  // record it before calling that function, since we need its
                  // value as a parameter to sendKeyEvent, below.
                  //
                  int repeatCode = lastPressedKeyRawCode;
                  this.stopKeyRepeatTimer();
                  this.sendKeyEvent( IHmeProtocol.KEY_RELEASE, repeatCode );
              }

              //
              // Start the repeat timer before we send the pressed key. This
              // way, if the key handler takes a long time to handle the event,
              // the repeat timer will fire 1000ms after the key was generated,
              // not 1000ms after the key handler returns.
              //
              this.startKeyRepeatTimerForKey( rawcode );
              this.sendKeyEvent( action, rawcode );

              return;
          }

          case IHmeProtocol.KEY_RELEASE:
          {
              if ( rawcode == lastPressedKeyRawCode )
              {
                  //
                  // This event might be a real key release, or it might be part
                  // of a repeat key. Start the release key timer to see if we
                  // get a PRESSED event immediately after the RELEASED
                  // event. If so, it's a repeat key.
                  //
                  this.startKeyReleaseTimer();
              }
              return;
          }

          default:
          {
              return;
          }
        }
    }

    /**
     * Send an HME key event to the active HME application.
     */
    private void sendKeyEvent( int action, int rawcode )
    {
        int keycode = this.getHmeKeyCodeForRawCode( rawcode );
        if ( keycode == IHmeProtocol.KEY_UNKNOWN )
        {
            // not a key we care about.
            return;
        }

        SimResource target = pane.getTargetForKey( keycode );

        if (target != null)
        {
            target.processEvent(new HmeEvent.Key(target.id, action, keycode, rawcode));
        }
    }

    /**
     * Map a raw key code to the corresponding HME key.
     */
    private int getHmeKeyCodeForRawCode( int rawcode )
    {
        int code = IHmeProtocol.KEY_UNKNOWN;
        switch (rawcode) {
          case KeyEvent.VK_UP:        code = IHmeProtocol.KEY_UP; break;
          case KeyEvent.VK_DOWN:      code = IHmeProtocol.KEY_DOWN; break;
          case KeyEvent.VK_LEFT:      code = IHmeProtocol.KEY_LEFT; break;
          case KeyEvent.VK_RIGHT:     code = IHmeProtocol.KEY_RIGHT; break;
          case KeyEvent.VK_PAGE_UP:   code = IHmeProtocol.KEY_CHANNELUP; break;
          case KeyEvent.VK_PAGE_DOWN: code = IHmeProtocol.KEY_CHANNELDOWN; break;
          case KeyEvent.VK_ENTER:     code = IHmeProtocol.KEY_SELECT; break;
          case KeyEvent.VK_0:         code = IHmeProtocol.KEY_NUM0; break;
          case KeyEvent.VK_1:         code = IHmeProtocol.KEY_NUM1; break;
          case KeyEvent.VK_2:         code = IHmeProtocol.KEY_NUM2; break;
          case KeyEvent.VK_3:         code = IHmeProtocol.KEY_NUM3; break;
          case KeyEvent.VK_4:         code = IHmeProtocol.KEY_NUM4; break;
          case KeyEvent.VK_5:         code = IHmeProtocol.KEY_NUM5; break;
          case KeyEvent.VK_6:         code = IHmeProtocol.KEY_NUM6; break;
          case KeyEvent.VK_7:         code = IHmeProtocol.KEY_NUM7; break;
          case KeyEvent.VK_8:         code = IHmeProtocol.KEY_NUM8; break;
          case KeyEvent.VK_9:         code = IHmeProtocol.KEY_NUM9; break;
          case 'P':                   code = IHmeProtocol.KEY_PLAY; break;
          case ' ':                   code = IHmeProtocol.KEY_PAUSE; break;
          case 'S':                   code = IHmeProtocol.KEY_SLOW; break;
          case '[':                   code = IHmeProtocol.KEY_REVERSE; break;
          case ']':                   code = IHmeProtocol.KEY_FORWARD; break;
          case '-':                   code = IHmeProtocol.KEY_REPLAY; break;
          case '=':                   code = IHmeProtocol.KEY_ADVANCE; break;
          case 'U':                   code = IHmeProtocol.KEY_THUMBSUP; break;
          case 'D':                   code = IHmeProtocol.KEY_THUMBSDOWN; break;
          case 'M':                   code = IHmeProtocol.KEY_MUTE; break;
          case 'R':                   code = IHmeProtocol.KEY_RECORD; break;
          case 'I':                   code = IHmeProtocol.KEY_INFO; break;
          case 'C':                   code = IHmeProtocol.KEY_CLEAR; break;
          case 'E':                   code = IHmeProtocol.KEY_ENTER; break;
          case 'W':                   code = IHmeProtocol.KEY_OPT_WINDOW; break;
          case 'X':                   code = IHmeProtocol.KEY_OPT_EXIT; break;
          case KeyEvent.VK_BACK_QUOTE:code = IHmeProtocol.KEY_OPT_STOP; break;
          case ',':                   code = IHmeProtocol.KEY_OPT_MENU; break;
          case '.':                   code = IHmeProtocol.KEY_OPT_TOP_MENU; break;
          case 'A':                   code = IHmeProtocol.KEY_OPT_ANGLE; break;
          default: break;
        }

        return code;
    }

    /**
     * We give this function the rawcode because in some tivo code, the repeat
     * times depend on the key being pressed.
     *
     * If HME ever follows suit, we can put the logic in this function.
     */
    private void startKeyRepeatTimerForKey( int rawcode )
    {
        this.stopKeyRepeatTimer();

        lastPressedKeyRawCode = rawcode;

        // the delay and initial delay are taken from the native receiver code
        keyRepeatTimer = new Timer( repeatTimerRate, this );
        keyRepeatTimer.setInitialDelay( repeatTimerInitial );
        keyRepeatTimer.setRepeats( true );
        keyRepeatTimer.start();
    }

    private void stopKeyRepeatTimer()
    {
        lastPressedKeyRawCode = IHmeProtocol.KEY_UNKNOWN;

        if ( keyRepeatTimer != null )
        {
            keyRepeatTimer.removeActionListener( this );
            keyRepeatTimer.stop();
            keyRepeatTimer = null;
        }
    }

    private void startKeyReleaseTimer()
    {
        this.stopKeyReleaseTimer();

        keyReleaseTimer = new Timer( releaseTimerRate, this );
        keyReleaseTimer.setRepeats( false );
        keyReleaseTimer.start();
    }

    private void stopKeyReleaseTimer()
    {
        if ( keyReleaseTimer != null )
        {
            keyReleaseTimer.removeActionListener( this );
            keyReleaseTimer.stop();
            keyReleaseTimer = null;
        }
    }

    /**
     * Stop repeating, and send a RELEASE event for the last pressed key if we
     * were in the midst of a repeat.
     */
    private void stop()
    {
        //
        // Make sure we don't repeat keys if we no longer have focus.
        //

        if ( this.getHmeKeyCodeForRawCode( lastPressedKeyRawCode ) !=
             IHmeProtocol.KEY_UNKNOWN )
        {
            //
            // If the last pressed key was anything other than UNKNOWN, a key
            // was being held down when we lost focus. Make sure we send a
            // RELEASE event now.
            //
            this.sendKeyEvent( IHmeProtocol.KEY_RELEASE, lastPressedKeyRawCode );
        }

        //
        // Shut down any active timers.
        //
        this.stopKeyRepeatTimer();
        this.stopKeyReleaseTimer();
    }

    /**
     * The timers call back here.
     */
    public void actionPerformed( ActionEvent e )
    {
        if ( !pane.hasFocus() )
        {
            this.stop();
            return;
        }

        if ( e.getSource() == keyRepeatTimer )
        {
            this.sendKeyEvent( IHmeProtocol.KEY_REPEAT, lastPressedKeyRawCode );
            return;
        }

        if ( e.getSource() == keyReleaseTimer )
        {
            this.stop();
            return;
        }
    }

    /**
     * Called whenever the pane loses focus.
     */
    public void focusLost( FocusEvent e )
    {
        if ( e.getSource() == pane )
        {
            this.stop();
        }
    }

    /**
     * Called whenever the pane gains focus.
     */
    public void focusGained( FocusEvent e )
    {
        // do nothing
    }
}
