package org.dazeend.harmonium.music;

import java.util.regex.Pattern;

public abstract class HMusic
{
	protected static final Pattern titlePattern = Pattern.compile("(?i)^(the|a|an)\\s");
}
