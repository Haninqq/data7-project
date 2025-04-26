package com.data7.instdesign.service;

import com.data7.instdesign.dto.mypage.SavedGoalsDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import com.data7.instdesign.mapper.MypageMapper;
import com.data7.instdesign.mapper.SearchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class MypageService {
    private final MypageMapper mypageMapper;

    public List<SavedGoalsDTO> getGoals(String userId){
        return mypageMapper.getGoals(userId);
    }
}
