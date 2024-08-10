package com.xclhove.consumer.springboot;

import cn.hutool.core.date.DateUtil;
import com.xclhove.common.model.Note;
import com.xclhove.common.model.User;
import com.xclhove.common.service.NoteService;
import com.xclhove.common.service.UserService;
import com.xclhove.rpc.springboot.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class ApplicationTest {
    @RpcReference
    private UserService userService;
    @RpcReference
    private NoteService noteService;
    
    @Test
    void consume() {
        User user = userService.getUser(new User().setName("user:consumer-test-springboot"));
        System.out.println(user);
        Note note = noteService.getNote("note:consumer-test-springboot");
        System.out.println(note);
    }
    
}
