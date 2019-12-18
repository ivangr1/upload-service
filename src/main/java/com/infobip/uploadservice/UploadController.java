package com.infobip.uploadservice;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executor;


@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    private Executor executor;
    private Map<String, Long> duration = new HashMap<>();

    @Autowired
    public UploadController(Executor executor) { this.executor = executor; }

    @PostMapping
    public @ResponseBody ResponseEntity uploadFiles(@RequestParam("files") MultipartFile[] files) {
        final long start = System.currentTimeMillis();
        Set<String> fileNames = new HashSet<>();
        try {
            for (final MultipartFile file : files) {
                String filename = StringUtils.cleanPath(file.getOriginalFilename());
                if(fileNames.contains(filename))
                    continue;
                else
                    fileNames.add(filename);
                executor.execute(() -> {
                    try {
                        byte[] bytes = file.getBytes();
                        Path path = Paths.get("upload-dir").resolve(filename);
                        Files.write(path, bytes);
                        long timestamp = System.currentTimeMillis();
                        duration.put(String.format("%s-%d", filename, timestamp), (timestamp - start));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/progress")
    public ResponseEntity uploadProgress() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/duration")
    public ResponseEntity<List<String>> uploadDuration() {
        List<String> duration = new ArrayList<>();
        this.duration.forEach((filename, dur) -> duration.add(String.format("upload_duration{id=”%s”} %d", filename, dur)));

        return ResponseEntity.status(HttpStatus.OK).body(duration);
    }
}