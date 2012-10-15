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

package org.apache.commons.net.ftp;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Formatter;

/***
 * The FTPFile class is used to represent information about files stored
 * on an FTP server.
 * <p>
 * <p>
 * @see FTPFileEntryParser
 * @see FTPClient#listFiles
 ***/

public class FTPFile implements Serializable
{
    private static final long serialVersionUID = 9010790363003271996L;

    /** A constant indicating an FTPFile is a file. ***/
    public static final int FILE_TYPE = 0;
    /** A constant indicating an FTPFile is a directory. ***/
    public static final int DIRECTORY_TYPE = 1;
    /** A constant indicating an FTPFile is a symbolic link. ***/
    public static final int SYMBOLIC_LINK_TYPE = 2;
    /** A constant indicating an FTPFile is of unknown type. ***/
    public static final int UNKNOWN_TYPE = 3;

    /** A constant indicating user access permissions. ***/
    public static final int USER_ACCESS = 0;
    /** A constant indicating group access permissions. ***/
    public static final int GROUP_ACCESS = 1;
    /** A constant indicating world access permissions. ***/
    public static final int WORLD_ACCESS = 2;

    /** A constant indicating file/directory read permission. ***/
    public static final int READ_PERMISSION = 0;
    /** A constant indicating file/directory write permission. ***/
    public static final int WRITE_PERMISSION = 1;
    /**
     * A constant indicating file execute permission or directory listing
     * permission.
     ***/
    public static final int EXECUTE_PERMISSION = 2;

    private int _type, _hardLinkCount;
    private long _size;
    private String _rawListing, _user, _group, _name, _link;
    private Calendar _date;
    private boolean[] _permissions[]; // e.g. _permissions[USER_ACCESS][READ_PERMISSION]

    /*** Creates an empty FTPFile. ***/
    public FTPFile()
    {
        _permissions = new boolean[3][3];
        _rawListing = null;
        _type = UNKNOWN_TYPE;
        // init these to values that do not occur in listings
        // so can distinguish which fields are unset
        _hardLinkCount = 0; // 0 is invalid as a link count
        _size = -1; // 0 is valid, so use -1
        _user = "";
        _group = "";
        _date = null;
        _name = null;
    }


    /***
     * Set the original FTP server raw listing from which the FTPFile was
     * created.
     * <p>
     * @param rawListing  The raw FTP server listing.
     ***/
    public void setRawListing(String rawListing)
    {
        _rawListing = rawListing;
    }

    /***
     * Get the original FTP server raw listing used to initialize the FTPFile.
     * <p>
     * @return The original FTP server raw listing used to initialize the
     *         FTPFile.
     ***/
    public String getRawListing()
    {
        return _rawListing;
    }


    /***
     * Determine if the file is a directory.
     * <p>
     * @return True if the file is of type <code>DIRECTORY_TYPE</code>, false if
     *         not.
     ***/
    public boolean isDirectory()
    {
        return (_type == DIRECTORY_TYPE);
    }

    /***
     * Determine if the file is a regular file.
     * <p>
     * @return True if the file is of type <code>FILE_TYPE</code>, false if
     *         not.
     ***/
    public boolean isFile()
    {
        return (_type == FILE_TYPE);
    }

    /***
     * Determine if the file is a symbolic link.
     * <p>
     * @return True if the file is of type <code>UNKNOWN_TYPE</code>, false if
     *         not.
     ***/
    public boolean isSymbolicLink()
    {
        return (_type == SYMBOLIC_LINK_TYPE);
    }

    /***
     * Determine if the type of the file is unknown.
     * <p>
     * @return True if the file is of type <code>UNKNOWN_TYPE</code>, false if
     *         not.
     ***/
    public boolean isUnknown()
    {
        return (_type == UNKNOWN_TYPE);
    }


    /***
     * Set the type of the file (<code>DIRECTORY_TYPE</code>,
     * <code>FILE_TYPE</code>, etc.).
     * <p>
     * @param type  The integer code representing the type of the file.
     ***/
    public void setType(int type)
    {
        _type = type;
    }


    /***
     * Return the type of the file (one of the <code>_TYPE</code> constants),
     * e.g., if it is a directory, a regular file, or a symbolic link.
     * <p>
     * @return The type of the file.
     ***/
    public int getType()
    {
        return _type;
    }


    /***
     * Set the name of the file.
     * <p>
     * @param name  The name of the file.
     ***/
    public void setName(String name)
    {
        _name = name;
    }

    /***
     * Return the name of the file.
     * <p>
     * @return The name of the file.
     ***/
    public String getName()
    {
        return _name;
    }


    /**
     * Set the file size in bytes.
     * @param size The file size in bytes.
     */
    public void setSize(long size)
    {
        _size = size;
    }


    /***
     * Return the file size in bytes.
     * <p>
     * @return The file size in bytes.
     ***/
    public long getSize()
    {
        return _size;
    }


    /***
     * Set the number of hard links to this file.  This is not to be
     * confused with symbolic links.
     * <p>
     * @param links  The number of hard links to this file.
     ***/
    public void setHardLinkCount(int links)
    {
        _hardLinkCount = links;
    }


    /***
     * Return the number of hard links to this file.  This is not to be
     * confused with symbolic links.
     * <p>
     * @return The number of hard links to this file.
     ***/
    public int getHardLinkCount()
    {
        return _hardLinkCount;
    }


    /***
     * Set the name of the group owning the file.  This may be
     * a string representation of the group number.
     * <p>
     * @param group The name of the group owning the file.
     ***/
    public void setGroup(String group)
    {
        _group = group;
    }


    /***
     * Returns the name of the group owning the file.  Sometimes this will be
     * a string representation of the group number.
     * <p>
     * @return The name of the group owning the file.
     ***/
    public String getGroup()
    {
        return _group;
    }


    /***
     * Set the name of the user owning the file.  This may be
     * a string representation of the user number;
     * <p>
     * @param user The name of the user owning the file.
     ***/
    public void setUser(String user)
    {
        _user = user;
    }

    /***
     * Returns the name of the user owning the file.  Sometimes this will be
     * a string representation of the user number.
     * <p>
     * @return The name of the user owning the file.
     ***/
    public String getUser()
    {
        return _user;
    }


    /***
     * If the FTPFile is a symbolic link, use this method to set the name of the
     * file being pointed to by the symbolic link.
     * <p>
     * @param link  The file pointed to by the symbolic link.
     ***/
    public void setLink(String link)
    {
        _link = link;
    }


    /***
     * If the FTPFile is a symbolic link, this method returns the name of the
     * file being pointed to by the symbolic link.  Otherwise it returns null.
     * <p>
     * @return The file pointed to by the symbolic link (null if the FTPFile
     *         is not a symbolic link).
     ***/
    public String getLink()
    {
        return _link;
    }


    /***
     * Set the file timestamp.  This usually the last modification time.
     * The parameter is not cloned, so do not alter its value after calling
     * this method.
     * <p>
     * @param date A Calendar instance representing the file timestamp.
     ***/
    public void setTimestamp(Calendar date)
    {
        _date = date;
    }


    /***
     * Returns the file timestamp.  This usually the last modification time.
     * <p>
     * @return A Calendar instance representing the file timestamp.
     ***/
    public Calendar getTimestamp()
    {
        return _date;
    }


    /***
     * Set if the given access group (one of the <code> _ACCESS </code>
     * constants) has the given access permission (one of the
     * <code> _PERMISSION </code> constants) to the file.
     * <p>
     * @param access The access group (one of the <code> _ACCESS </code>
     *               constants)
     * @param permission The access permission (one of the
     *               <code> _PERMISSION </code> constants)
     * @param value  True if permission is allowed, false if not.
     ***/
    public void setPermission(int access, int permission, boolean value)
    {
        _permissions[access][permission] = value;
    }


    /***
     * Determines if the given access group (one of the <code> _ACCESS </code>
     * constants) has the given access permission (one of the
     * <code> _PERMISSION </code> constants) to the file.
     * <p>
     * @param access The access group (one of the <code> _ACCESS </code>
     *               constants)
     * @param permission The access permission (one of the
     *               <code> _PERMISSION </code> constants)
     ***/
    public boolean hasPermission(int access, int permission)
    {
        return _permissions[access][permission];
    }

    /***
     * Returns a string representation of the FTPFile information.
     *
     * @return A string representation of the FTPFile information.
     */
    @Override
    public String toString()
    {
        return getRawListing();
    }

    /***
     * Returns a string representation of the FTPFile information.
     * This currently mimics the Unix listing format.
     *
     * @return A string representation of the FTPFile information.
     * @since 3.0
     */
    public String toFormattedString()
    {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        sb.append(formatType());
        sb.append(permissionToString(USER_ACCESS));
        sb.append(permissionToString(GROUP_ACCESS));
        sb.append(permissionToString(WORLD_ACCESS));
        fmt.format(" %4d", Integer.valueOf(getHardLinkCount()));
        fmt.format(" %-8s %-8s", getGroup(), getUser());
        fmt.format(" %8d", Long.valueOf(getSize()));
        Calendar timestamp = getTimestamp();
        if (timestamp != null) {
            fmt.format(" %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", timestamp);
            fmt.format(" %1$tZ", timestamp);
            sb.append(' ');
        }
        sb.append(' ');
        sb.append(getName());
        return sb.toString();
    }

    private char formatType(){
        switch(_type) {
            case FILE_TYPE:
                return '-';
            case DIRECTORY_TYPE:
                return 'd';
            case SYMBOLIC_LINK_TYPE:
                return 'l';
        }
        return '?';
    }

    private String permissionToString(int access ){
        StringBuilder sb = new StringBuilder();
        if (hasPermission(access, READ_PERMISSION)) {
            sb.append('r');
        } else {
            sb.append('-');
        }
        if (hasPermission(access, WRITE_PERMISSION)) {
            sb.append('w');
        } else {
            sb.append('-');
        }
        if (hasPermission(access, EXECUTE_PERMISSION)) {
            sb.append('x');
        } else {
            sb.append('-');
        }
        return sb.toString();
    }
}
