package com.example.online_job_protal.Service;

import com.example.online_job_protal.Model.FileModel;

import com.example.online_job_protal.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private  String uploadDir;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;

        // Create the upload directory if it doesn't exist
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public FileModel uploadFile(MultipartFile file) throws IOException {
        // Ensure the upload directory exists
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();  // Create the directory if it doesn't exist
        }

        // Generate a unique file name to avoid conflicts
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String fileType = file.getContentType();
        String fileUrl = Paths.get(uploadDir, fileName).toString();

        // Save the file to the specified directory
        Path path = Paths.get(fileUrl);
        Files.copy(file.getInputStream(), path);

        // Create a FileModel to store the file's metadata
        FileModel fileModel = new FileModel();
        fileModel.setResumeFileName(fileName);
        fileModel.setResumeFileType(fileType);
        fileModel.setResumeFileUrl(fileUrl);

        // Save file metadata in the database
        return fileRepository.save(fileModel);
    }

    public FileModel getFile(Long id) {
        return fileRepository.findById(id).orElse(null);
    }

    public List<FileModel> getAllFiles() {
        return fileRepository.findAll(); // Fetch all files from the database
    }
}
