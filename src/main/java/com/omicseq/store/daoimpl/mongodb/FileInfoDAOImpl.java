package com.omicseq.store.daoimpl.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.omicseq.common.FileInfoStatus;
import com.omicseq.common.SortType;
import com.omicseq.domain.FileInfo;
import com.omicseq.store.dao.IFileInfoDAO;
import com.omicseq.store.helper.MongodbHelper;

/**
 * 
 * @author zejun.du
 */
public class FileInfoDAOImpl extends GenericMongoDBDAO<FileInfo> implements IFileInfoDAO {

    @Override
    public FileInfo get(String url) {
        return super.findOne(new SmartDBObject("url", url));
    }
    

    @Override
    public FileInfo getBySampleId(Integer sampleId) {
        return findOne(new SmartDBObject("sampleId", sampleId));
    }


    @Override
    public void clean() {
        super.delete(new SmartDBObject());
    }

    @Override
    public List<FileInfo> findUndownload(String server, int limit) {
        // where state=0 or (server=? and state>1 and state<5);
        SmartDBObject query = new SmartDBObject("state", FileInfoStatus.INIT.value());
        if (StringUtils.isNotBlank(server)) {
            SmartDBObject sec = MongodbHelper.and(new SmartDBObject(),
                    MongodbHelper.lt("state", FileInfoStatus.DOWNLOADED.value()));
            query = MongodbHelper.or(query, sec);
        }
        query.put("source", 12);
        query.put("url",new SmartDBObject("$regex",".txt"));
        query.addSort("priority", SortType.DESC);
        query.addSort("sampleId", SortType.ASC);
        return super.find(query, 0, limit);
    }

    @Override
    public List<FileInfo> findDownload(String server, int start, int limit) {
        if (StringUtils.isBlank(server)) {
            return new ArrayList<FileInfo>(0);
        }
        Integer state = FileInfoStatus.DOWNLOADED.value();
        SmartDBObject query = MongodbHelper.and(new SmartDBObject("server", server), new SmartDBObject("state", state));
        return super.find(query, start, limit);
    }

}
