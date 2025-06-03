package com.koko.mywiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.koko.mywiki.domain.Doc;
import com.koko.mywiki.domain.DocExample;
import com.koko.mywiki.mapper.DocMapper;
import com.koko.mywiki.req.DocQueryReq;
import com.koko.mywiki.req.DocSaveReq;
import com.koko.mywiki.resp.DocQueryResp;
import com.koko.mywiki.resp.PageResp;
import com.koko.mywiki.until.CopyUtil;
import com.koko.mywiki.until.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocService {

    private static final Logger LOG = LoggerFactory.getLogger(DocService.class);

    @Resource
    private DocMapper docMapper;

    @Resource
    private SnowFlake snowFlake;

    public List<DocQueryResp> all() {
        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        List<Doc> docList = docMapper.selectByExample(docExample);

        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);
        return list;
    }


    public PageResp<DocQueryResp> list(DocQueryReq req) {
        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        DocExample.Criteria criteria = docExample.createCriteria();
        PageHelper.startPage(req.getPage(), req.getSize() );
        List<Doc> docList = docMapper.selectByExample(docExample);

        PageInfo<Doc> pageinfo = new PageInfo<>(docList);
        LOG.info("总行数：{}" ,pageinfo.getTotal());
        LOG.info("总列数：{}", pageinfo.getPages());

        List<DocQueryResp> respList = new ArrayList<>();
        for (Doc doc : docList) {
//            DocResp docResp = new DocResp();
//            BeanUtils.copyProperties(doc, docResp);

            DocQueryResp docResp = CopyUtil.copy(doc, DocQueryResp.class);
            respList.add(docResp);
        }

        List<DocQueryResp> list = CopyUtil.copyList(docList, DocQueryResp.class);
        PageResp<DocQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageinfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    /*
    * 保存
    * */
    public void save(DocSaveReq req) {
        Doc doc = CopyUtil.copy(req, Doc.class);
        if (ObjectUtils.isEmpty(doc.getId())) {
            // 新增
            doc.setId(snowFlake.nextId());
            docMapper.insert(doc);
        }else {
            // 更新
            docMapper.updateByPrimaryKey(doc);
        }
    }

    /*
     * 删除
     * */
    public void delete(Long id) {
        docMapper.deleteByPrimaryKey(id);
    }
}
