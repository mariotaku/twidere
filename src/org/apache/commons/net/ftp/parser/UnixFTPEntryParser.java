/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.ftp.parser;
import java.text.ParseException;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Implementation FTPFileEntryParser and FTPFileListParser for standard
 * Unix Systems.
 *
 * This class is based on the logic of Daniel Savarese's
 * DefaultFTPListParser, but adapted to use regular expressions and to fit the
 * new FTPFileEntryParser interface.
 * @version $Id: UnixFTPEntryParser.java 1243400 2012-02-13 03:02:29Z sebb $
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class UnixFTPEntryParser extends ConfigurableFTPFileEntryParserImpl
{

    static final String DEFAULT_DATE_FORMAT
        = "MMM d yyyy"; //Nov 9 2001

    static final String DEFAULT_RECENT_DATE_FORMAT
        = "MMM d HH:mm"; //Nov 9 20:06

    static final String NUMERIC_DATE_FORMAT
        = "yyyy-MM-dd HH:mm"; //2001-11-09 20:06

    /**
     * Some Linux distributions are now shipping an FTP server which formats
     * file listing dates in an all-numeric format:
     * <code>"yyyy-MM-dd HH:mm</code>.
     * This is a very welcome development,  and hopefully it will soon become
     * the standard.  However, since it is so new, for now, and possibly
     * forever, we merely accomodate it, but do not make it the default.
     * <p>
     * For now end users may specify this format only via
     * <code>UnixFTPEntryParser(FTPClientConfig)</code>.
     * Steve Cohen - 2005-04-17
     */
    public static final FTPClientConfig NUMERIC_DATE_CONFIG =
        new FTPClientConfig(
                FTPClientConfig.SYST_UNIX,
                NUMERIC_DATE_FORMAT,
                null, null, null, null);

    /**
     * this is the regular expression used by this parser.
     *
     * Permissions:
     *    r   the file is readable
     *    w   the file is writable
     *    x   the file is executable
     *    -   the indicated permission is not granted
     *    L   mandatory locking occurs during access (the set-group-ID bit is
     *        on and the group execution bit is off)
     *    s   the set-user-ID or set-group-ID bit is on, and the corresponding
     *        user or group execution bit is also on
     *    S   undefined bit-state (the set-user-ID bit is on and the user
     *        execution bit is off)
     *    t   the 1000 (octal) bit, or sticky bit, is on [see chmod(1)], and
     *        execution is on
     *    T   the 1000 bit is turned on, and execution is off (undefined bit-
     *        state)
     *    e   z/OS external link bit
     */
    private static final String REGEX =
        "([bcdelfmpSs-])"
        +"(((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-])))\\+?\\s*"
        + "(\\d+)\\s+"                                  // link count
        + "(?:(\\S+(?:\\s\\S+)*?)\\s+)?"                // owner name (optional spaces)
        + "(?:(\\S+(?:\\s\\S+)*)\\s+)?"                 // group name (optional spaces)
        + "(\\d+(?:,\\s*\\d+)?)\\s+"                    // size or n,m
        /*
         * numeric or standard format date:
         *   yyyy-mm-dd (expecting hh:mm to follow)
         *   MMM [d]d
         *   [d]d MMM
         *   N.B. use non-space for MMM to allow for languages such as German which use
         *   diacritics (e.g. umlaut) in some abbreviations.
        */
        + "((?:\\d+[-/]\\d+[-/]\\d+)|(?:\\S{3}\\s+\\d{1,2})|(?:\\d{1,2}\\s+\\S{3}))\\s+"
        /*
           year (for non-recent standard format) - yyyy
           or time (for numeric or recent standard format) [h]h:mm
        */
        + "(\\d+(?::\\d+)?)\\s+"

        + "(\\S*)(\\s*.*)"; // the rest


    /**
     * The default constructor for a UnixFTPEntryParser object.
     *
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public UnixFTPEntryParser()
    {
        this(null);
    }

    /**
     * This constructor allows the creation of a UnixFTPEntryParser object with
     * something other than the default configuration.
     *
     * @param config The {@link FTPClientConfig configuration} object used to
     * configure this parser.
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     * @since 1.4
     */
    public UnixFTPEntryParser(FTPClientConfig config)
    {
        super(REGEX);
        configure(config);
    }

    /**
     * Preparse the list to discard "total nnn" lines
     */
    @Override
    public List<String> preParse(List<String> original) {
        ListIterator<String> iter = original.listIterator();
        while (iter.hasNext()) {
            String entry = iter.next();
            if (entry.matches("^total \\d+$")) { // NET-389
                iter.remove();
            }
        }
        return original;
    }

    /**
     * Parses a line of a unix (standard) FTP server file listing and converts
     * it into a usable format in the form of an <code> FTPFile </code>
     * instance.  If the file listing line doesn't describe a file,
     * <code> null </code> is returned, otherwise a <code> FTPFile </code>
     * instance representing the files in the directory is returned.
     * <p>
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public FTPFile parseFTPEntry(String entry) {
        FTPFile file = new FTPFile();
        file.setRawListing(entry);
        int type;
        boolean isDevice = false;

        if (matches(entry))
        {
            String typeStr = group(1);
            String hardLinkCount = group(15);
            String usr = group(16);
            String grp = group(17);
            String filesize = group(18);
            String datestr = group(19) + " " + group(20);
            String name = group(21);
            String endtoken = group(22);

            try
            {
                file.setTimestamp(super.parseTimestamp(datestr));
            }
            catch (ParseException e)
            {
                 // intentionally do nothing
            }

            // A 'whiteout' file is an ARTIFICIAL entry in any of several types of
            // 'translucent' filesystems, of which a 'union' filesystem is one.

            // bcdelfmpSs-
            switch (typeStr.charAt(0))
            {
            case 'd':
                type = FTPFile.DIRECTORY_TYPE;
                break;
            case 'e': // NET-39 => z/OS external link
                type = FTPFile.SYMBOLIC_LINK_TYPE;
                break;
            case 'l':
                type = FTPFile.SYMBOLIC_LINK_TYPE;
                break;
            case 'b':
            case 'c':
                isDevice = true;
                type = FTPFile.FILE_TYPE; // TODO change this if DEVICE_TYPE implemented
                break;
            case 'f':
            case '-':
                type = FTPFile.FILE_TYPE;
                break;
            default: // e.g. ? and w = whiteout
                type = FTPFile.UNKNOWN_TYPE;
            }

            file.setType(type);

            int g = 4;
            for (int access = 0; access < 3; access++, g += 4)
            {
                // Use != '-' to avoid having to check for suid and sticky bits
                file.setPermission(access, FTPFile.READ_PERMISSION,
                                   (!group(g).equals("-")));
                file.setPermission(access, FTPFile.WRITE_PERMISSION,
                                   (!group(g + 1).equals("-")));

                String execPerm = group(g + 2);
                if (!execPerm.equals("-") && !Character.isUpperCase(execPerm.charAt(0)))
                {
                    file.setPermission(access, FTPFile.EXECUTE_PERMISSION, true);
                }
                else
                {
                    file.setPermission(access, FTPFile.EXECUTE_PERMISSION, false);
                }
            }

            if (!isDevice)
            {
                try
                {
                    file.setHardLinkCount(Integer.parseInt(hardLinkCount));
                }
                catch (NumberFormatException e)
                {
                    // intentionally do nothing
                }
            }

            file.setUser(usr);
            file.setGroup(grp);

            try
            {
                file.setSize(Long.parseLong(filesize));
            }
            catch (NumberFormatException e)
            {
                // intentionally do nothing
            }

            if (null == endtoken)
            {
                file.setName(name);
            }
            else
            {
                // oddball cases like symbolic links, file names
                // with spaces in them.
                name += endtoken;
                if (type == FTPFile.SYMBOLIC_LINK_TYPE)
                {

                    int end = name.indexOf(" -> ");
                    // Give up if no link indicator is present
                    if (end == -1)
                    {
                        file.setName(name);
                    }
                    else
                    {
                        file.setName(name.substring(0, end));
                        file.setLink(name.substring(end + 4));
                    }

                }
                else
                {
                    file.setName(name);
                }
            }
            return file;
        }
        return null;
    }

    /**
     * Defines a default configuration to be used when this class is
     * instantiated without a {@link  FTPClientConfig  FTPClientConfig}
     * parameter being specified.
     * @return the default configuration for this parser.
     */
    @Override
    protected FTPClientConfig getDefaultConfiguration() {
        return new FTPClientConfig(
                FTPClientConfig.SYST_UNIX,
                DEFAULT_DATE_FORMAT,
                DEFAULT_RECENT_DATE_FORMAT,
                null, null, null);
    }

}
