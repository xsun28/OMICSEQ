package com.omicseq.store.dao;

import com.omicseq.domain.CronTask;

public interface ICronTaskDAO extends IGenericDAO<CronTask> {

    CronTask get(String name, String launchServer);

}
