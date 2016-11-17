package com.omicseq.store.imp;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import com.omicseq.core.GeneCache;
import com.omicseq.domain.GeneRank;
import com.omicseq.store.dao.IGeneRankDAO;
import com.omicseq.store.daoimpl.factory.DAOFactory;
import com.omicseq.store.daoimpl.mongodb.SmartDBObject;
import com.omicseq.utils.DateUtils;
import com.omicseq.utils.JSONUtils;

public class MongoDBToSql {

    public static void main(String[] args) throws IOException {
        Random rn = new Random();
        File file = new File("./generank.sql");
        IGeneRankDAO dao = DAOFactory.getDAO(IGeneRankDAO.class);
        final StringBuilder headBuilder = new StringBuilder();
        headBuilder.append("INSERT INTO `");
        headBuilder.append("example");
        headBuilder.append("` (`");
        headBuilder.append("id,HashId,Content,ExpiryDate,LastUpdatedTime".replaceAll(" ", "").replaceAll(",", "`,`"));
        headBuilder.append("` ) VALUES ");
        StringBuilder sqlBuilder = new StringBuilder();
        for (int id = 1; id < 50; id++) {
            sqlBuilder.append(headBuilder.toString());
            SmartDBObject query = new SmartDBObject("geneId", id);
            List<GeneRank> coll = dao.find(query);
            if (CollectionUtils.isEmpty(coll)) {
                continue;
            }
            int length = sqlBuilder.length(), count = 0;
            for (GeneRank GeneRank : coll) {
                if (length > 10485760) {
                    sqlBuilder.append(";\r\n");
                    sqlBuilder.append(headBuilder.toString());
                    length = sqlBuilder.length();
                    count = 0;
                } else if (count > 0) {
                    sqlBuilder.append(",");
                    length++;
                }
                count++;
                sqlBuilder.append("(");
                String json = JSONUtils.to(GeneRank).replaceAll("\"", "'");
                String time = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                StringBuilder sb = new StringBuilder();
                sb.append("\"").append(rn.nextLong()).append("\",");
                sb.append("\"").append("generank_" + GeneRank.get_id()).append("\",");
                sb.append("\"").append(json).append("\",");
                sb.append("\"").append(time).append("\",");
                sb.append("\"").append(time).append("\"");
                String record = sb.toString();
                sqlBuilder.append(record);
                sqlBuilder.append(")");
                length += 2;
                length += record.length();
            }
            sqlBuilder.append(";\r\n");
            if (id % 10 == 0) {
                FileUtils.write(file, sqlBuilder, true);
                sqlBuilder.delete(0, sqlBuilder.length());
            }
        }
        FileUtils.write(file, sqlBuilder, true);
    }
}
