package com.omicseq.robot.store;

import java.util.Arrays;
import java.util.List;

import com.omicseq.common.SourceType;
import com.omicseq.domain.Chunk;
import com.omicseq.domain.FileInfo;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;

public class FileInfoTest {

    public static void main(String[] args) {
        IFileInfoDAO dao = DAOFactory.getInstance().getDAO(IFileInfoDAO.class);
        System.out.println("clean ");
        dao.clean();

        FileInfo info = new FileInfo();
        info.setUrl(SourceType.TCGA.url());
        info.setLength(Long.valueOf(1024 * 1024));
        info.setState(0);
        info.setLastModified(System.currentTimeMillis());
        info.setChunks(Arrays.asList(new Chunk(0l, info.getLength())));
        dao.create(info);
        System.out.println();

        FileInfo obj = dao.get(info.getUrl());
        System.out.println("get");
        System.out.println(obj);

        System.out.println("all");
        List<FileInfo> coll = dao.findAll();

        for (FileInfo item : coll) {
            System.out.println(item);
        }

    }
}
