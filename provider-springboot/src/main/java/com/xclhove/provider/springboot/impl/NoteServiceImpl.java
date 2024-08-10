package com.xclhove.provider.springboot.impl;

import com.xclhove.common.model.Note;
import com.xclhove.common.service.NoteService;
import com.xclhove.rpc.springboot.annotation.RpcService;

/**
 * @author xclhove
 */
@RpcService
public class NoteServiceImpl implements NoteService {
    @Override
    public Note getNote(String title) {
        return new Note().setTitle(title).setContent(title);
    }
}
