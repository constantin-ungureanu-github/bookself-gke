package com.example.getstarted.util;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class CloudStorageHelper {
    private final Logger logger = Logger.getLogger(CloudStorageHelper.class.getName());
    private static Storage storage = null;

    static {
        storage = StorageOptions.getDefaultInstance().getService();
    }

    /**
     * Upload file.
     *
     * @param filePart
     *            the file part
     * @param bucketName
     *            the bucket name
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("deprecation")
    public String uploadFile(final Part filePart, final String bucketName) throws IOException {
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("-YYYY-MM-dd-HHmmssSSS");
        final DateTime dt = DateTime.now(DateTimeZone.UTC);
        final String dtString = dt.toString(dtf);
        final String fileName = filePart.getSubmittedFileName() + dtString;

        // the inputstream is closed by default, so we don't need to close it here
        final BlobInfo blobInfo = storage.create(BlobInfo.newBuilder(bucketName, fileName)
                // Modify access list to allow all users with link to read file
                .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER)))).build(), filePart.getInputStream());
        logger.log(Level.INFO, "Uploaded file {0} as {1}", new Object[] { filePart.getSubmittedFileName(), fileName });
        // return the public download link
        return blobInfo.getMediaLink();
    }

    /**
     * Gets the image url.
     *
     * @param req
     *            the req
     * @param resp
     *            the resp
     * @param bucket
     *            the bucket
     * @return the image url
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ServletException
     *             the servlet exception
     */
    public String getImageUrl(final HttpServletRequest req, final HttpServletResponse resp, final String bucket) throws IOException, ServletException {
        final Part filePart = req.getPart("file");
        final String fileName = filePart.getSubmittedFileName();
        final String imageUrl = req.getParameter("imageUrl");
        // Check extension of file
        if (fileName != null && !fileName.isEmpty() && fileName.contains(".")) {
            final String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
            final String[] allowedExt = { "jpg", "jpeg", "png", "gif" };
            for (final String s : allowedExt) {
                if (extension.equals(s)) {
                    return uploadFile(filePart, bucket);
                }
            }
            throw new ServletException("file must be an image");
        }
        return imageUrl;
    }
}
