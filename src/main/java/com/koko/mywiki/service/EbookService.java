package com.koko.mywiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.koko.mywiki.domain.Ebook;
import com.koko.mywiki.domain.EbookExample;
import com.koko.mywiki.mapper.EbookMapper;
import com.koko.mywiki.req.EbookQueryReq;
import com.koko.mywiki.req.EbookSaveReq;
import com.koko.mywiki.resp.EbookResp;
import com.koko.mywiki.resp.PageResp;
import com.koko.mywiki.util.CopyUtil;
import com.koko.mywiki.util.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class EbookService {

    private static final Logger LOG = LoggerFactory.getLogger(EbookService.class);

    @Resource
    private EbookMapper ebookMapper;

    @Resource
    private SnowFlake snowFlake;

    public PageResp<EbookResp> list(EbookQueryReq req) {
        EbookExample ebookExample = new EbookExample();
        EbookExample.Criteria criteria = ebookExample.createCriteria();
        if(!ObjectUtils.isEmpty(req.getName())) {
            criteria.andNameLike("%" + req.getName() + "%");
        }
        if(!ObjectUtils.isEmpty(req.getCategoryId2())) {
            criteria.andCategory2IdEqualTo(req.getCategoryId2());
        }
        PageHelper.startPage(req.getPage(), req.getSize() );
        List<Ebook> ebookList = ebookMapper.selectByExample(ebookExample);

        PageInfo<Ebook> pageinfo = new PageInfo<>(ebookList);
        LOG.info("总行数：{}" ,pageinfo.getTotal());
        LOG.info("总列数：{}", pageinfo.getPages());

        List<EbookResp> respList = new ArrayList<>();
        for (Ebook ebook : ebookList) {
//            EbookResp ebookResp = new EbookResp();
//            BeanUtils.copyProperties(ebook, ebookResp);

            EbookResp ebookResp = CopyUtil.copy(ebook, EbookResp.class);
            respList.add(ebookResp);
        }

        List<EbookResp> list = CopyUtil.copyList(ebookList, EbookResp.class);
        PageResp<EbookResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageinfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    /*
    * 保存
    * */
    public void save(EbookSaveReq req) {
        Ebook ebook = CopyUtil.copy(req, Ebook.class);
        if (ObjectUtils.isEmpty(ebook.getId())) {
            // 新增
            ebook.setId(snowFlake.nextId());
            ebookMapper.insert(ebook);
        }else {
            // 更新
            ebookMapper.updateByPrimaryKey(ebook);
        }
    }

    /*
     * 删除
     * */
    public void delete(Long id) {
        ebookMapper.deleteByPrimaryKey(id);
    }
}
