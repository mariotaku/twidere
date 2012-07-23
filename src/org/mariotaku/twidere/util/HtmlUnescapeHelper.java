package org.mariotaku.twidere.util;

/*
 * Static String formatting and query routines.
 * Copyright (C) 2001-2005 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */

import java.util.HashMap;

/**
 * Utilities for String formatting, manipulation, and queries. More information
 * about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/HtmlUnescapeHelper.html">ostermiller.org</a>.
 * 
 * @author Stephen Ostermiller
 *         http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class HtmlUnescapeHelper {
	private static HashMap<String, Integer> htmlEntities = new HashMap<String, Integer>();
	static {
		htmlEntities.put("nbsp", 160);
		htmlEntities.put("iexcl", 161);
		htmlEntities.put("cent", 162);
		htmlEntities.put("pound", 163);
		htmlEntities.put("curren", 164);
		htmlEntities.put("yen", 165);
		htmlEntities.put("brvbar", 166);
		htmlEntities.put("sect", 167);
		htmlEntities.put("uml", 168);
		htmlEntities.put("copy", 169);
		htmlEntities.put("ordf", 170);
		htmlEntities.put("laquo", 171);
		htmlEntities.put("not", 172);
		htmlEntities.put("shy", 173);
		htmlEntities.put("reg", 174);
		htmlEntities.put("macr", 175);
		htmlEntities.put("deg", 176);
		htmlEntities.put("plusmn", 177);
		htmlEntities.put("sup2", 178);
		htmlEntities.put("sup3", 179);
		htmlEntities.put("acute", 180);
		htmlEntities.put("micro", 181);
		htmlEntities.put("para", 182);
		htmlEntities.put("middot", 183);
		htmlEntities.put("cedil", 184);
		htmlEntities.put("sup1", 185);
		htmlEntities.put("ordm", 186);
		htmlEntities.put("raquo", 187);
		htmlEntities.put("frac14", 188);
		htmlEntities.put("frac12", 189);
		htmlEntities.put("frac34", 190);
		htmlEntities.put("iquest", 191);
		htmlEntities.put("Agrave", 192);
		htmlEntities.put("Aacute", 193);
		htmlEntities.put("Acirc", 194);
		htmlEntities.put("Atilde", 195);
		htmlEntities.put("Auml", 196);
		htmlEntities.put("Aring", 197);
		htmlEntities.put("AElig", 198);
		htmlEntities.put("Ccedil", 199);
		htmlEntities.put("Egrave", 200);
		htmlEntities.put("Eacute", 201);
		htmlEntities.put("Ecirc", 202);
		htmlEntities.put("Euml", 203);
		htmlEntities.put("Igrave", 204);
		htmlEntities.put("Iacute", 205);
		htmlEntities.put("Icirc", 206);
		htmlEntities.put("Iuml", 207);
		htmlEntities.put("ETH", 208);
		htmlEntities.put("Ntilde", 209);
		htmlEntities.put("Ograve", 210);
		htmlEntities.put("Oacute", 211);
		htmlEntities.put("Ocirc", 212);
		htmlEntities.put("Otilde", 213);
		htmlEntities.put("Ouml", 214);
		htmlEntities.put("times", 215);
		htmlEntities.put("Oslash", 216);
		htmlEntities.put("Ugrave", 217);
		htmlEntities.put("Uacute", 218);
		htmlEntities.put("Ucirc", 219);
		htmlEntities.put("Uuml", 220);
		htmlEntities.put("Yacute", 221);
		htmlEntities.put("THORN", 222);
		htmlEntities.put("szlig", 223);
		htmlEntities.put("agrave", 224);
		htmlEntities.put("aacute", 225);
		htmlEntities.put("acirc", 226);
		htmlEntities.put("atilde", 227);
		htmlEntities.put("auml", 228);
		htmlEntities.put("aring", 229);
		htmlEntities.put("aelig", 230);
		htmlEntities.put("ccedil", 231);
		htmlEntities.put("egrave", 232);
		htmlEntities.put("eacute", 233);
		htmlEntities.put("ecirc", 234);
		htmlEntities.put("euml", 235);
		htmlEntities.put("igrave", 236);
		htmlEntities.put("iacute", 237);
		htmlEntities.put("icirc", 238);
		htmlEntities.put("iuml", 239);
		htmlEntities.put("eth", 240);
		htmlEntities.put("ntilde", 241);
		htmlEntities.put("ograve", 242);
		htmlEntities.put("oacute", 243);
		htmlEntities.put("ocirc", 244);
		htmlEntities.put("otilde", 245);
		htmlEntities.put("ouml", 246);
		htmlEntities.put("divide", 247);
		htmlEntities.put("oslash", 248);
		htmlEntities.put("ugrave", 249);
		htmlEntities.put("uacute", 250);
		htmlEntities.put("ucirc", 251);
		htmlEntities.put("uuml", 252);
		htmlEntities.put("yacute", 253);
		htmlEntities.put("thorn", 254);
		htmlEntities.put("yuml", 255);
		htmlEntities.put("fnof", 402);
		htmlEntities.put("Alpha", 913);
		htmlEntities.put("Beta", 914);
		htmlEntities.put("Gamma", 915);
		htmlEntities.put("Delta", 916);
		htmlEntities.put("Epsilon", 917);
		htmlEntities.put("Zeta", 918);
		htmlEntities.put("Eta", 919);
		htmlEntities.put("Theta", 920);
		htmlEntities.put("Iota", 921);
		htmlEntities.put("Kappa", 922);
		htmlEntities.put("Lambda", 923);
		htmlEntities.put("Mu", 924);
		htmlEntities.put("Nu", 925);
		htmlEntities.put("Xi", 926);
		htmlEntities.put("Omicron", 927);
		htmlEntities.put("Pi", 928);
		htmlEntities.put("Rho", 929);
		htmlEntities.put("Sigma", 931);
		htmlEntities.put("Tau", 932);
		htmlEntities.put("Upsilon", 933);
		htmlEntities.put("Phi", 934);
		htmlEntities.put("Chi", 935);
		htmlEntities.put("Psi", 936);
		htmlEntities.put("Omega", 937);
		htmlEntities.put("alpha", 945);
		htmlEntities.put("beta", 946);
		htmlEntities.put("gamma", 947);
		htmlEntities.put("delta", 948);
		htmlEntities.put("epsilon", 949);
		htmlEntities.put("zeta", 950);
		htmlEntities.put("eta", 951);
		htmlEntities.put("theta", 952);
		htmlEntities.put("iota", 953);
		htmlEntities.put("kappa", 954);
		htmlEntities.put("lambda", 955);
		htmlEntities.put("mu", 956);
		htmlEntities.put("nu", 957);
		htmlEntities.put("xi", 958);
		htmlEntities.put("omicron", 959);
		htmlEntities.put("pi", 960);
		htmlEntities.put("rho", 961);
		htmlEntities.put("sigmaf", 962);
		htmlEntities.put("sigma", 963);
		htmlEntities.put("tau", 964);
		htmlEntities.put("upsilon", 965);
		htmlEntities.put("phi", 966);
		htmlEntities.put("chi", 967);
		htmlEntities.put("psi", 968);
		htmlEntities.put("omega", 969);
		htmlEntities.put("thetasym", 977);
		htmlEntities.put("upsih", 978);
		htmlEntities.put("piv", 982);
		htmlEntities.put("bull", 8226);
		htmlEntities.put("hellip", 8230);
		htmlEntities.put("prime", 8242);
		htmlEntities.put("Prime", 8243);
		htmlEntities.put("oline", 8254);
		htmlEntities.put("frasl", 8260);
		htmlEntities.put("weierp", 8472);
		htmlEntities.put("image", 8465);
		htmlEntities.put("real", 8476);
		htmlEntities.put("trade", 8482);
		htmlEntities.put("alefsym", 8501);
		htmlEntities.put("larr", 8592);
		htmlEntities.put("uarr", 8593);
		htmlEntities.put("rarr", 8594);
		htmlEntities.put("darr", 8595);
		htmlEntities.put("harr", 8596);
		htmlEntities.put("crarr", 8629);
		htmlEntities.put("lArr", 8656);
		htmlEntities.put("uArr", 8657);
		htmlEntities.put("rArr", 8658);
		htmlEntities.put("dArr", 8659);
		htmlEntities.put("hArr", 8660);
		htmlEntities.put("forall", 8704);
		htmlEntities.put("part", 8706);
		htmlEntities.put("exist", 8707);
		htmlEntities.put("empty", 8709);
		htmlEntities.put("nabla", 8711);
		htmlEntities.put("isin", 8712);
		htmlEntities.put("notin", 8713);
		htmlEntities.put("ni", 8715);
		htmlEntities.put("prod", 8719);
		htmlEntities.put("sum", 8721);
		htmlEntities.put("minus", 8722);
		htmlEntities.put("lowast", 8727);
		htmlEntities.put("radic", 8730);
		htmlEntities.put("prop", 8733);
		htmlEntities.put("infin", 8734);
		htmlEntities.put("ang", 8736);
		htmlEntities.put("and", 8743);
		htmlEntities.put("or", 8744);
		htmlEntities.put("cap", 8745);
		htmlEntities.put("cup", 8746);
		htmlEntities.put("int", 8747);
		htmlEntities.put("there4", 8756);
		htmlEntities.put("sim", 8764);
		htmlEntities.put("cong", 8773);
		htmlEntities.put("asymp", 8776);
		htmlEntities.put("ne", 8800);
		htmlEntities.put("equiv", 8801);
		htmlEntities.put("le", 8804);
		htmlEntities.put("ge", 8805);
		htmlEntities.put("sub", 8834);
		htmlEntities.put("sup", 8835);
		htmlEntities.put("nsub", 8836);
		htmlEntities.put("sube", 8838);
		htmlEntities.put("supe", 8839);
		htmlEntities.put("oplus", 8853);
		htmlEntities.put("otimes", 8855);
		htmlEntities.put("perp", 8869);
		htmlEntities.put("sdot", 8901);
		htmlEntities.put("lceil", 8968);
		htmlEntities.put("rceil", 8969);
		htmlEntities.put("lfloor", 8970);
		htmlEntities.put("rfloor", 8971);
		htmlEntities.put("lang", 9001);
		htmlEntities.put("rang", 9002);
		htmlEntities.put("loz", 9674);
		htmlEntities.put("spades", 9824);
		htmlEntities.put("clubs", 9827);
		htmlEntities.put("hearts", 9829);
		htmlEntities.put("diams", 9830);
		htmlEntities.put("quot", 34);
		htmlEntities.put("amp", 38);
		htmlEntities.put("lt", 60);
		htmlEntities.put("gt", 62);
		htmlEntities.put("OElig", 338);
		htmlEntities.put("oelig", 339);
		htmlEntities.put("Scaron", 352);
		htmlEntities.put("scaron", 353);
		htmlEntities.put("Yuml", 376);
		htmlEntities.put("circ", 710);
		htmlEntities.put("tilde", 732);
		htmlEntities.put("ensp", 8194);
		htmlEntities.put("emsp", 8195);
		htmlEntities.put("thinsp", 8201);
		htmlEntities.put("zwnj", 8204);
		htmlEntities.put("zwj", 8205);
		htmlEntities.put("lrm", 8206);
		htmlEntities.put("rlm", 8207);
		htmlEntities.put("ndash", 8211);
		htmlEntities.put("mdash", 8212);
		htmlEntities.put("lsquo", 8216);
		htmlEntities.put("rsquo", 8217);
		htmlEntities.put("sbquo", 8218);
		htmlEntities.put("ldquo", 8220);
		htmlEntities.put("rdquo", 8221);
		htmlEntities.put("bdquo", 8222);
		htmlEntities.put("dagger", 8224);
		htmlEntities.put("Dagger", 8225);
		htmlEntities.put("permil", 8240);
		htmlEntities.put("lsaquo", 8249);
		htmlEntities.put("rsaquo", 8250);
		htmlEntities.put("euro", 8364);
	}

	/**
	 * Turn any HTML escape entities in the string into characters and return
	 * the resulting string.
	 * 
	 * @param s String to be unescaped.
	 * @return unescaped String.
	 * @throws NullPointerException if s is null.
	 * 
	 * @since ostermillerutils 1.00.00
	 */
	public static String unescapeHTML(String s) {
		if (s == null) return null;
		s = s.replaceAll("<!--.*?--> |<[^>]+>", "");
		final StringBuffer result = new StringBuffer(s.length());
		int ampInd = s.indexOf("&");
		int lastEnd = 0;
		while (ampInd >= 0) {
			final int nextAmp = s.indexOf("&", ampInd + 1);
			final int nextSemi = s.indexOf(";", ampInd + 1);
			if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)) {
				int value = -1;
				final String escape = s.substring(ampInd + 1, nextSemi);
				try {
					if (escape.startsWith("#")) {
						value = Integer.parseInt(escape.substring(1), 10);
					} else {
						if (htmlEntities.containsKey(escape)) {
							value = htmlEntities.get(escape);
						}
					}
				} catch (final NumberFormatException e) {
					// Ignore.
				}
				result.append(s.substring(lastEnd, ampInd));
				lastEnd = nextSemi + 1;
				if (value >= 0 && value <= 0xffff) {
					result.append((char) value);
				} else {
					result.append("&").append(escape).append(";");
				}
			}
			ampInd = nextAmp;
		}
		result.append(s.substring(lastEnd));
		return result.toString();
	}
}