package com.infobip.uploadservice;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private Map<String, Long> duration = new HashMap<>();
    private Map<String, Map<String, Object>> progressMap = new HashMap<>();

    @PostMapping
    public ResponseEntity uploadFiles(final HttpServletRequest request) {
        final long start = System.currentTimeMillis();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if(!isMultipart) ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE);
        String fileName = request.getHeader("X-Upload-File");
        String contentLength = request.getHeader("Content-Length");
        String fileId = String.format("%s-%d", fileName, start);
        Map<String, Object> progress = new TreeMap<>();
        progress.put("id", fileId);
        progress.put("size", Integer.parseInt(contentLength));
        progress.put("uploaded", 0);
        progressMap.put(fileId, progress);
        try {
            ServletFileUpload servletFileUpload = new ServletFileUpload();
            servletFileUpload.setProgressListener(new ProgressListener() {
                long megaBytes = -1;
                @Override
                public void update(long bytesRead, long bytesTotal, int item) {
                    long mBytes = bytesRead / 100;
                    if (megaBytes == mBytes) {
                        return;
                    }
                    megaBytes = mBytes;
                    if (bytesRead == bytesTotal) {
                        UploadController.this.progressMap.remove(fileId);
                        UploadController.this.duration.put(fileId, (System.currentTimeMillis() - start));
                    } else
                        UploadController.this.progressMap.get(fileId).replace("uploaded", bytesRead);
                }
            });
            FileItemIterator iterStream = servletFileUpload.getItemIterator(request);
            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                if (!item.isFormField()) {
                    try (InputStream uploadedStream = item.openStream()) {
                        Files.copy(uploadedStream, Paths.get("upload-dir").resolve(fileName),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch(FileUploadException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(contentLength);
    }

    @GetMapping("/progress")
    public Map<String, Object> uploadProgress() {
        Map<String, Object> map = new HashMap<>();
        map.put("uploads", new ArrayList<>(this.progressMap.values()));
        return map;
    }

    @GetMapping("/duration")
    public Map<String, Map<String, Long>> uploadDuration() {
        Map<String, Map<String, Long>> map = new HashMap<>();
        map.put("upload_duration", duration);
        return map;
    }
}