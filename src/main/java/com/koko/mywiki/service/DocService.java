package com.koko.mywiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.koko.mywiki.domain.Content;
import com.koko.mywiki.domain.Doc;
import com.koko.mywiki.domain.DocExample;
import com.koko.mywiki.exception.BusinessException;
import com.koko.mywiki.exception.BusinessExceptionCode;
import com.koko.mywiki.mapper.ContentMapper;
import com.koko.mywiki.mapper.DocMapper;
import com.koko.mywiki.mapper.DocMapperCust;
import com.koko.mywiki.req.DocQueryReq;
import com.koko.mywiki.req.DocSaveReq;
import com.koko.mywiki.resp.DocQueryResp;
import com.koko.mywiki.resp.PageResp;
import com.koko.mywiki.util.CopyUtil;
import com.koko.mywiki.util.RedisUtil;
import com.koko.mywiki.util.RequestContext;
import com.koko.mywiki.util.SnowFlake;
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
    private DocMapperCust docMapperCust;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private SnowFlake snowFlake;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    public WsService wsService;

    public List<DocQueryResp> all(Long ebookId) {
        DocExample docExample = new DocExample();
        docExample.createCriteria().andEbookIdEqualTo(ebookId);
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
        Content content = CopyUtil.copy(req, Content.class);
        if (ObjectUtils.isEmpty(doc.getId())) {
            // 新增
            doc.setId(snowFlake.nextId());
            doc.setViewCount(0);
            doc.setVoteCount(0);
            docMapper.insert(doc);

            content.setId(doc.getId());
            contentMapper.insert(content);
        }else {
            // 更新
            docMapper.updateByPrimaryKey(doc);
            int count = contentMapper.updateByPrimaryKeyWithBLOBs(content);
            if (count == 0){
                contentMapper.insert(content);
            }
        }
    }

    /*
     * 删除
     * */
    public void delete(Long id) {
        docMapper.deleteByPrimaryKey(id);
    }

    /*
     * 删除重载
     * */
    public void delete(List<String> ids) {
        DocExample docExample = new DocExample();
        DocExample.Criteria criteria = docExample.createCriteria();
        criteria.andIdIn(ids);
        docMapper.deleteByExample(docExample);
    }

    /*
     * 查找
     * */
    public String findContent(Long id) {
        Content content = contentMapper.selectByPrimaryKey(id);
        //文档阅读数+1
        docMapperCust.increaseViewCount(id);
        if (ObjectUtils.isEmpty(content)) {
            return "";
        }else{
            return content.getContent();
        }
    }

    /*
     * 点赞
     * */
    public void vote(Long id) {
        // docMapperCust.increaseVoteCount(id);
        // 远程IP+doc.id作为key，24小时内不能重复
        String ip = RequestContext.getRemoteAddr();
        if (redisUtil.validateRepeat("DOC_VOTE_" + id + "_" + ip, 3600 * 24)) {
            docMapperCust.increaseVoteCount(id);
        } else {
            throw new BusinessException(BusinessExceptionCode.VOTE_REPEAT);

        }

        // 推送消息
        Doc docDb = docMapper.selectByPrimaryKey(id);
//        String logId = MDC.get("LOG_ID");
        wsService.sendInfo("【" + docDb.getName() + "】被点赞！");
    }

    public void updateEbookInfo() {
        docMapperCust.updateEbookInfo();
    }
}
