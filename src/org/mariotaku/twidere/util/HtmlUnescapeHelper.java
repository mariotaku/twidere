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

	@SuppressWarnings("serial")
	private static final HashMap<String, Integer> htmlEntities = new HashMap<String, Integer>() {
		{
			put("nbsp", 160);
			put("iexcl", 161);
			put("cent", 162);
			put("pound", 163);
			put("curren", 164);
			put("yen", 165);
			put("brvbar", 166);
			put("sect", 167);
			put("uml", 168);
			put("copy", 169);
			put("ordf", 170);
			put("laquo", 171);
			put("not", 172);
			put("shy", 173);
			put("reg", 174);
			put("macr", 175);
			put("deg", 176);
			put("plusmn", 177);
			put("sup2", 178);
			put("sup3", 179);
			put("acute", 180);
			put("micro", 181);
			put("para", 182);
			put("middot", 183);
			put("cedil", 184);
			put("sup1", 185);
			put("ordm", 186);
			put("raquo", 187);
			put("frac14", 188);
			put("frac12", 189);
			put("frac34", 190);
			put("iquest", 191);
			put("Agrave", 192);
			put("Aacute", 193);
			put("Acirc", 194);
			put("Atilde", 195);
			put("Auml", 196);
			put("Aring", 197);
			put("AElig", 198);
			put("Ccedil", 199);
			put("Egrave", 200);
			put("Eacute", 201);
			put("Ecirc", 202);
			put("Euml", 203);
			put("Igrave", 204);
			put("Iacute", 205);
			put("Icirc", 206);
			put("Iuml", 207);
			put("ETH", 208);
			put("Ntilde", 209);
			put("Ograve", 210);
			put("Oacute", 211);
			put("Ocirc", 212);
			put("Otilde", 213);
			put("Ouml", 214);
			put("times", 215);
			put("Oslash", 216);
			put("Ugrave", 217);
			put("Uacute", 218);
			put("Ucirc", 219);
			put("Uuml", 220);
			put("Yacute", 221);
			put("THORN", 222);
			put("szlig", 223);
			put("agrave", 224);
			put("aacute", 225);
			put("acirc", 226);
			put("atilde", 227);
			put("auml", 228);
			put("aring", 229);
			put("aelig", 230);
			put("ccedil", 231);
			put("egrave", 232);
			put("eacute", 233);
			put("ecirc", 234);
			put("euml", 235);
			put("igrave", 236);
			put("iacute", 237);
			put("icirc", 238);
			put("iuml", 239);
			put("eth", 240);
			put("ntilde", 241);
			put("ograve", 242);
			put("oacute", 243);
			put("ocirc", 244);
			put("otilde", 245);
			put("ouml", 246);
			put("divide", 247);
			put("oslash", 248);
			put("ugrave", 249);
			put("uacute", 250);
			put("ucirc", 251);
			put("uuml", 252);
			put("yacute", 253);
			put("thorn", 254);
			put("yuml", 255);
			put("fnof", 402);
			put("Alpha", 913);
			put("Beta", 914);
			put("Gamma", 915);
			put("Delta", 916);
			put("Epsilon", 917);
			put("Zeta", 918);
			put("Eta", 919);
			put("Theta", 920);
			put("Iota", 921);
			put("Kappa", 922);
			put("Lambda", 923);
			put("Mu", 924);
			put("Nu", 925);
			put("Xi", 926);
			put("Omicron", 927);
			put("Pi", 928);
			put("Rho", 929);
			put("Sigma", 931);
			put("Tau", 932);
			put("Upsilon", 933);
			put("Phi", 934);
			put("Chi", 935);
			put("Psi", 936);
			put("Omega", 937);
			put("alpha", 945);
			put("beta", 946);
			put("gamma", 947);
			put("delta", 948);
			put("epsilon", 949);
			put("zeta", 950);
			put("eta", 951);
			put("theta", 952);
			put("iota", 953);
			put("kappa", 954);
			put("lambda", 955);
			put("mu", 956);
			put("nu", 957);
			put("xi", 958);
			put("omicron", 959);
			put("pi", 960);
			put("rho", 961);
			put("sigmaf", 962);
			put("sigma", 963);
			put("tau", 964);
			put("upsilon", 965);
			put("phi", 966);
			put("chi", 967);
			put("psi", 968);
			put("omega", 969);
			put("thetasym", 977);
			put("upsih", 978);
			put("piv", 982);
			put("bull", 8226);
			put("hellip", 8230);
			put("prime", 8242);
			put("Prime", 8243);
			put("oline", 8254);
			put("frasl", 8260);
			put("weierp", 8472);
			put("image", 8465);
			put("real", 8476);
			put("trade", 8482);
			put("alefsym", 8501);
			put("larr", 8592);
			put("uarr", 8593);
			put("rarr", 8594);
			put("darr", 8595);
			put("harr", 8596);
			put("crarr", 8629);
			put("lArr", 8656);
			put("uArr", 8657);
			put("rArr", 8658);
			put("dArr", 8659);
			put("hArr", 8660);
			put("forall", 8704);
			put("part", 8706);
			put("exist", 8707);
			put("empty", 8709);
			put("nabla", 8711);
			put("isin", 8712);
			put("notin", 8713);
			put("ni", 8715);
			put("prod", 8719);
			put("sum", 8721);
			put("minus", 8722);
			put("lowast", 8727);
			put("radic", 8730);
			put("prop", 8733);
			put("infin", 8734);
			put("ang", 8736);
			put("and", 8743);
			put("or", 8744);
			put("cap", 8745);
			put("cup", 8746);
			put("int", 8747);
			put("there4", 8756);
			put("sim", 8764);
			put("cong", 8773);
			put("asymp", 8776);
			put("ne", 8800);
			put("equiv", 8801);
			put("le", 8804);
			put("ge", 8805);
			put("sub", 8834);
			put("sup", 8835);
			put("nsub", 8836);
			put("sube", 8838);
			put("supe", 8839);
			put("oplus", 8853);
			put("otimes", 8855);
			put("perp", 8869);
			put("sdot", 8901);
			put("lceil", 8968);
			put("rceil", 8969);
			put("lfloor", 8970);
			put("rfloor", 8971);
			put("lang", 9001);
			put("rang", 9002);
			put("loz", 9674);
			put("spades", 9824);
			put("clubs", 9827);
			put("hearts", 9829);
			put("diams", 9830);
			put("quot", 34);
			put("amp", 38);
			put("lt", 60);
			put("gt", 62);
			put("OElig", 338);
			put("oelig", 339);
			put("Scaron", 352);
			put("scaron", 353);
			put("Yuml", 376);
			put("circ", 710);
			put("tilde", 732);
			put("ensp", 8194);
			put("emsp", 8195);
			put("thinsp", 8201);
			put("zwnj", 8204);
			put("zwj", 8205);
			put("lrm", 8206);
			put("rlm", 8207);
			put("ndash", 8211);
			put("mdash", 8212);
			put("lsquo", 8216);
			put("rsquo", 8217);
			put("sbquo", 8218);
			put("ldquo", 8220);
			put("rdquo", 8221);
			put("bdquo", 8222);
			put("dagger", 8224);
			put("Dagger", 8225);
			put("permil", 8240);
			put("lsaquo", 8249);
			put("rsaquo", 8250);
			put("euro", 8364);
		}
	};

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
		s = s.replaceAll("<br\\/>", "\n").replaceAll("<!--.*?--> |<[^>]+>", "");
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
