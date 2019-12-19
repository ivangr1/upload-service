package com.infobip.uploadservice;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;


@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);
    private Executor executor;
    private Map<String, Long> duration = new HashMap<>();
    private Map<String, Map<String, Object>> progressMap = new HashMap<>();

    @Autowired
    public UploadController(Executor executor) { this.executor = executor; }

    @PostMapping
    public ResponseEntity uploadFiles(final HttpServletRequest request) {
        final long start = System.currentTimeMillis();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if(!isMultipart) ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE);
        String fileName = request.getHeader("X-Upload-File");
        String contentLength = request.getHeader("Content-Length");
        String fileId = fileName + "-" + start;
        Map<String, Object> progress = new TreeMap<>();
        progress.put("id", fileId);
        progress.put("size", Integer.parseInt(contentLength));
        progress.put("uploaded", 0);
        progressMap.put(fileId, progress);
        try {
            ServletFileUpload servletFileUpload = new ServletFileUpload();
            servletFileUpload.setProgressListener((l, l1, i) -> {
                if(l == l1) {
                    this.progressMap.remove(fileId);
                    this.duration.put(fileId, (System.currentTimeMillis() - start));
                }
                else
                    this.progressMap.get(fileId).replace("uploaded", l);
            });
            FileItemIterator iterStream = servletFileUpload.getItemIterator(request);
            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                if (!item.isFormField()) {
                    try (InputStream uploadedStream = item.openStream();
                            OutputStream out = new FileOutputStream("upload-dir/" + fileName)) {
                        IOUtils.copy(uploadedStream, out);
                    }
                }
            }
        } catch(FileUploadException | IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(contentLength);
    }

    @GetMapping("/progress")
    public Map<String, Object> uploadProgress() {
        Map<String, Object> map = new HashMap<>();
        map.put("uploads", new ArrayList<>(this.progressMap.values()).toArray());
        return map;
    }

    @GetMapping("/duration")
    public List<String> uploadDuration() {
        List<String> duration = new ArrayList<>();
        this.duration.forEach((filename, dur) -> duration.add(String.format("upload_duration{id=”%s”} %d", filename, dur)));
        return duration;
    }
}