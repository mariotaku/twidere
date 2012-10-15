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
import java.util.Calendar;

import org.apache.commons.net.ftp.FTPFile;

/**
 * Parser for the Connect Enterprise Unix FTP Server From Sterling Commerce.
 * Here is a sample of the sort of output line this parser processes:
 *  "-C--E-----FTP B QUA1I1      18128       41 Aug 12 13:56 QUADTEST"
 * <P><B>
 * Note: EnterpriseUnixFTPEntryParser can only be instantiated through the
 * DefaultFTPParserFactory by classname.  It will not be chosen
 * by the autodetection scheme.
 * </B>
 * @version $Id: EnterpriseUnixFTPEntryParser.java 658518 2008-05-21 01:04:30Z sebb $
 * @author <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
 */
public class EnterpriseUnixFTPEntryParser extends RegexFTPFileEntryParserImpl
{

    /**
     * months abbreviations looked for by this parser.  Also used
     * to determine <b>which</b> month has been matched by the parser.
     */
    private static final String MONTHS =
        "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";

    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
        "(([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])"
        + "([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z]))"
        + "(\\S*)\\s*"
        + "(\\S+)\\s*"
        + "(\\S*)\\s*"
        + "(\\d*)\\s*"
        + "(\\d*)\\s*"
        + MONTHS
        + "\\s*"
        + "((?:[012]\\d*)|(?:3[01]))\\s*"
        + "((\\d\\d\\d\\d)|((?:[01]\\d)|(?:2[0123])):([012345]\\d))\\s"
        + "(\\S*)(\\s*.*)";

    /**
     * The sole constructor for a EnterpriseUnixFTPEntryParser object.
     *
     */
    public EnterpriseUnixFTPEntryParser()
    {
        super(REGEX);
    }

    /**
     * Parses a line of a unix FTP server file listing and converts  it into a
     * usable format in the form of an <code> FTPFile </code>  instance.  If
     * the file listing line doesn't describe a file,  <code> null </code> is
     * returned, otherwise a <code> FTPFile </code>  instance representing the
     * files in the directory is returned.
     *
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public FTPFile parseFTPEntry(String entry)
    {

        FTPFile file = new FTPFile();
        file.setRawListing(entry);

        if (matches(entry))
        {
            String usr = group(14);
            String grp = group(15);
            String filesize = group(16);
            String mo = group(17);
            String da = group(18);
            String yr = group(20);
            String hr = group(21);
            String min = group(22);
            String name = group(23);

            file.setType(FTPFile.FILE_TYPE);
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

            Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND,
                    0);
            cal.set(Calendar.MINUTE,
                    0);
            cal.set(Calendar.HOUR_OF_DAY,
                    0);
            try
            {

                int pos = MONTHS.indexOf(mo);
                int month = pos / 4;
                if (yr != null)
                {
                    // it's a year
                    cal.set(Calendar.YEAR,
                            Integer.parseInt(yr));
                }
                else
                {
                    // it must be  hour/minute or we wouldn't have matched
                    int year = cal.get(Calendar.YEAR);

                    // if the month we're reading is greater than now, it must
                    // be last year
                    if (cal.get(Calendar.MONTH) < month)
                    {
                        year--;
                    }
                    cal.set(Calendar.YEAR,
                            year);
                    cal.set(Calendar.HOUR_OF_DAY,
                            Integer.parseInt(hr));
                    cal.set(Calendar.MINUTE,
                            Integer.parseInt(min));
                }
                cal.set(Calendar.MONTH,
                        month);
                cal.set(Calendar.DATE,
                        Integer.parseInt(da));
                file.setTimestamp(cal);
            }
            catch (NumberFormatException e)
            {
                // do nothing, date will be uninitialized
            }
            file.setName(name);

            return file;
        }
        return null;
    }
}
