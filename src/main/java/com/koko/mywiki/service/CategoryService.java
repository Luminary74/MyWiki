package com.koko.mywiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.koko.mywiki.domain.Category;
import com.koko.mywiki.domain.CategoryExample;
import com.koko.mywiki.mapper.CategoryMapper;
import com.koko.mywiki.req.CategoryQueryReq;
import com.koko.mywiki.req.CategorySaveReq;
import com.koko.mywiki.resp.CategoryQueryResp;
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
public class CategoryService {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryService.class);

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SnowFlake snowFlake;

    public List<CategoryQueryResp> all() {
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.setOrderByClause("sort asc");
        List<Category> categoryList = categoryMapper.selectByExample(categoryExample);

        List<CategoryQueryResp> list = CopyUtil.copyList(categoryList, CategoryQueryResp.class);
        return list;
    }


    public PageResp<CategoryQueryResp> list(CategoryQueryReq req) {
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.setOrderByClause("sort asc");
        CategoryExample.Criteria criteria = categoryExample.createCriteria();
        PageHelper.startPage(req.getPage(), req.getSize() );
        List<Category> categoryList = categoryMapper.selectByExample(categoryExample);

        PageInfo<Category> pageinfo = new PageInfo<>(categoryList);
        LOG.info("总行数：{}" ,pageinfo.getTotal());
        LOG.info("总列数：{}", pageinfo.getPages());

        List<CategoryQueryResp> respList = new ArrayList<>();
        for (Category category : categoryList) {
//            CategoryResp categoryResp = new CategoryResp();
//            BeanUtils.copyProperties(category, categoryResp);

            CategoryQueryResp categoryResp = CopyUtil.copy(category, CategoryQueryResp.class);
            respList.add(categoryResp);
        }

        List<CategoryQueryResp> list = CopyUtil.copyList(categoryList, CategoryQueryResp.class);
        PageResp<CategoryQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageinfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    /*
    * 保存
    * */
    public void save(CategorySaveReq req) {
        Category category = CopyUtil.copy(req, Category.class);
        if (ObjectUtils.isEmpty(category.getId())) {
            // 新增
            category.setId(snowFlake.nextId());
            categoryMapper.insert(category);
        }else {
            // 更新
            categoryMapper.updateByPrimaryKey(category);
        }
    }

    /*
     * 删除
     * */
    public void delete(Long id) {
        categoryMapper.deleteByPrimaryKey(id);
    }
}
