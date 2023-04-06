package com.task.demo.task;

import com.task.demo.task.model.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

public interface fileService {
    public Map<String[], Long> findAllSelectedPairs(Pair pair);
    public Set<String> getAllEmplIds(MultipartFile file);
}
