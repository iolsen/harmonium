package org.dazeend.harmonium.music;

import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.roarsoftware.lastfm.Track;

import org.dazeend.harmonium.Harmonium;
import org.dazeend.harmonium.ImageHelper;
import org.dazeend.harmonium.LastFm;

public class StreamTrackData
{
	private final Harmonium _app;
	
	private String _tagParsedStreamTitle;
	private String _tagParsedStreamUrl;

	private static final Pattern TAG_PARSE_TITLE_PATTERN = Pattern.compile("(.+)\\b\\s*-\\s*(.+)\\b");
	private static final Pattern TAG_PARSE_TITLE_WITH_PARENS_PATTERN = Pattern.compile("(.+)\\b\\s*-\\s*(.*)(?:\\s+\\(.*\\))");

	private static final int NEXT_TAG_DELAY = 500;
	private final Timer _timer = new Timer("WaitForNextTagEvent", true);
	private Runner _runner = null;
	private boolean _waitingForTag = false;

	public StreamTrackData(Harmonium app)
	{
		_app = app;
	}
	
	private synchronized void waitYourTurn() 
	{
		if (_runner != null)
		{
			// If the runner's already running, let it finish.
			while (_runner.isRunning())
			{
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					// do nothing
				}
			}
			_runner.cancel();
		}
		
		_runner = new Runner();
		
		if (!_waitingForTag)
		{
			_tagParsedStreamTitle = null;
			_tagParsedStreamUrl = null;
		}

		_waitingForTag = !_waitingForTag;
		_timer.schedule(_runner, NEXT_TAG_DELAY);
	}
	
	public synchronized void setTagParsedStreamTitle(String streamTitle) 
	{
		String fixed = streamTitle.replace('`', '\'');
		waitYourTurn();
		_tagParsedStreamTitle = fixed;
	}
	
	public synchronized void setTagParsedStreamUrl(String streamUrl)
	{
		waitYourTurn();
		_tagParsedStreamUrl = streamUrl;
	}

	private class Runner extends TimerTask
	{
		private boolean _running = false;

		private Image _img;
		private String _artHashKey;

		private String _tagParsedArtist;
		private String _tagParsedTrackName;

		public boolean isRunning() { return _running; }
		
		@Override
		public void run()
		{
			_running = true;
			try
			{
				realRun();
			}
			finally
			{
				_running = false;
			}
		}
		
		private void realRun()
		{
			Track track = null;
			
			getArtFromUrl();
				
			if (_tagParsedStreamTitle != null)
			{
				Matcher m = TAG_PARSE_TITLE_WITH_PARENS_PATTERN.matcher(_tagParsedStreamTitle);
				track = parseArtistAndTrack(m);
				if (track == null)
				{
					m = TAG_PARSE_TITLE_PATTERN.matcher(_tagParsedStreamTitle);
					track = parseArtistAndTrack(m);
				}

				if (_app.isInDebugMode())
				{
					if (track == null)
						System.out.println("Failed to retrieve last.fm info for stream: " + _tagParsedStreamTitle);
					else
					{
						System.out.println("Successfully retrieved last.fm info for stream: " + _tagParsedStreamTitle);
					}
				}
			}
			
			BasicArtSource art = null;
			if (_img != null)
				art = new BasicArtSource(_img, _artHashKey);
			else
				art = new BasicArtSource(null, null);
			
			if (track != null)
				_app.getDiscJockey().streamTrackDataChanged(art, track.getArtist(), _tagParsedArtist, track.getAlbum(), _tagParsedTrackName, 0);
			else
				_app.getDiscJockey().streamTrackDataChanged(art, _tagParsedArtist, _tagParsedArtist, null, _tagParsedTrackName, 0);
			_app.flush();
		}

		private Track parseArtistAndTrack(Matcher m)
		{
			if (_tagParsedStreamTitle == null || _tagParsedStreamTitle.isEmpty())
				return null;
			
			if (m.lookingAt())
			{
				if (_app.isInDebugMode())
				{
					System.out.println("Parsed Artist: [" + m.group(1) + "]");
					System.out.println(" Parsed Track: [" + m.group(2) + "]");
				}
				Track lastFmTrack = LastFm.fetchTrackInfo(m.group(1), m.group(2));
				
				if (lastFmTrack != null)
				{
					_tagParsedArtist = m.group(1);
					_tagParsedTrackName = m.group(2);

					if (_img == null)
					{
						_img = LastFm.fetchArtForTrack(lastFmTrack);
						if (ImageHelper.imageIsValid(_img))
							_artHashKey = _tagParsedStreamTitle;
						else
							_img = null;
					}
					return lastFmTrack;
				}
			}
			return null;
		}

		private void getArtFromUrl()
		{
			if (_tagParsedStreamUrl == null || _tagParsedStreamUrl.isEmpty())
				return;
			
			_img = ImageHelper.getImageFromUrl(_app, _tagParsedStreamUrl);
			if (_img != null)
				_artHashKey = _tagParsedStreamUrl;
		}
	}
}
