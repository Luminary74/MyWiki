package com.koko.mywiki.service;

import com.koko.mywiki.mapper.EbookSnapshotMapperCust;
import com.koko.mywiki.resp.StatisticResp;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EbookSnapshotService {

    @Resource
    private EbookSnapshotMapperCust ebookSnapshotMapperCust;

    public void genSnapshot() {
        ebookSnapshotMapperCust.genSnapshot();
    }

    /**
     * 获取首页数值数据：总阅读数、总点赞数、今日阅读数、今日点赞数、今日预计阅读数、今日预计阅读增长
     */
    public List<StatisticResp> getStatistic() {
        return ebookSnapshotMapperCust.getStatistic();
    }

    /**
     *   30天数值统计
     */
    public List<StatisticResp> get30Statistic() {
        return ebookSnapshotMapperCust.get30Statistic();
    }

}
