<template>
  <a-layout>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <p>
        <a-form layout="inline" :model="param">
          <a-form-item>
            <a-input v-model:value="param.name" placeholder="名称">
            </a-input>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery({page: 1, size: pagination.pageSize})">
              查询
            </a-button>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="add()">
              新增
            </a-button>
          </a-form-item>
        </a-form>
      </p>
      <a-table
          :columns="columns"
          :row-key="record => record.id"
          :data-source="ebooks"
          :pagination="pagination"
          :loading="loading"
          @change="handleTableChange"
      >
        <template #cover="{ text: cover }">
          <img v-if="cover" :src="cover" alt="avatar"/>
        </template>
        <template v-slot:category="{ text, record }">
          <span>{{ getCategoryName(record.category1Id) }} / {{ getCategoryName(record.category2Id) }}</span>
        </template>
        <template v-slot:action="{ text, record }">
          <a-space size="small">
            <router-link :to="'/admin/doc?ebookId=' + record.id">
              <a-button type="primary">
                文档管理
              </a-button>
            </router-link>
            <a-button type="primary" @click="edit(record)">
              编辑
            </a-button>
            <a-popconfirm
                title="删除后不可恢复，确认删除?"
                ok-text="是"
                cancel-text="否"
                @confirm="handleDelete(record.id)"
            >
              <a-button type="danger">
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-layout-content>
  </a-layout>

  <a-modal
      title="电子书表单"
      v-model:visible="modalVisible"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
  >
    <a-form :model="ebook" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="封面">
        <a-input v-model:value="ebook.cover" />
      </a-form-item>
      <a-form-item label="名称">
        <a-input v-model:value="ebook.name" />
      </a-form-item>
      <a-form-item label="分类">
        <a-cascader
          v-model:value="categoryIds"
          :field-names="{label:'name',value:'id',children:'children'}"
          :options="level1"
        />
      </a-form-item>
      <a-form-item label="描述">
        <a-input v-model:value="ebook.description" type="textarea" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
  import {defineComponent, onMounted, ref} from 'vue';
  import axios from 'axios';
  import { message } from 'ant-design-vue';
  import { Tool } from "@/util/tool";

  export default defineComponent({
    name: 'AdminEbook',
    setup() {
      const param = ref();
      param.value = {};
      const ebooks = ref();
      const pagination = ref({
        current: 1,
        pageSize: 10,
        total: 0
      });
      const loading = ref(false);

      const columns = [
        {
          title: '封面',
          dataIndex: 'cover',
          slots: {customRender: 'cover'}
        },
        {
          title: '名称',
          dataIndex: 'name'
        },
        {
          title: '分类',
          slots: { customRender: 'category' }
        },
        {
          title: '文档数',
          dataIndex: 'docCount'
        },
        {
          title: '阅读数',
          dataIndex: 'viewCount'
        },
        {
          title: '点赞数',
          dataIndex: 'voteCount'
        },
        {
          title: 'Action',
          key: 'action',
          slots: {customRender: 'action'}
        }
      ];


      /**
     * 数据查询
     **/
      const handleQuery = (params: any) => {
      loading.value = true;
      // 如果不清空现有数据，则编辑保存重新加载数据后，再点编辑，则列表显示的还是编辑前的数据
      ebooks.value = [];
      axios.get("/ebook/list", {
        params: {
          page: params.page,
          size: params.size,
          name: param.value.name
        }
      }).then((response) => {
        loading.value = false;
        const data = response.data;
        if (data.success) {
          ebooks.value = data.content.list;

          // 重置分页按钮
          pagination.value.current = params.page;
          pagination.value.total = data.content.total;
        } else {
          message.error(data.message);
        }
      });
    };

      /**
     * 表格点击页码时触发
     */
      const handleTableChange = (pagination: any) => {
      console.log("看看自带的分页参数都有啥：" + pagination);
      handleQuery({
        page: pagination.current,
        size: pagination.pageSize
      });
    };

      // -------- 表单 ---------
      /**
       * 数组，[100, 101]对应：前端开发 / Vue
       */
      const categoryIds = ref();
      const ebook = ref();
      const modalVisible = ref(false);
      const modalLoading = ref(false);
      const handleModalOk = () => {
        modalLoading.value = true;
        ebook.value.category1Id = categoryIds.value[0];
        ebook.value.category2Id = categoryIds.value[1];
        axios.post("/ebook/save", ebook.value).then((response) => {
          modalLoading.value = false;
          const data = response.data; // data = commonResp
          if (data.success) {
            modalVisible.value = false;
            modalLoading.value = false;

            // 重新加载列表
            handleQuery({
              page: pagination.value.current,
              size: pagination.value.pageSize,
            });
          }else {
            message.error(data.message);
          }
        });
      };


      /**
       * 编辑
       */
      const edit = (record: any) => {
        modalVisible.value = true;
        ebook.value = Tool.copy(record);
        categoryIds.value = [ebook.value.category1Id, ebook.value.category2Id];
      };

      /**
       * 新增
       */
      const add = () => {
        modalVisible.value = true;
        ebook.value = {};
      };

      const level1 =  ref();
      let categorys: any;
      /**
       * 查询所有分类
       **/
      const handleQueryCategory = () => {
        loading.value = true;
        axios.get("/category/all").then((response) => {
          loading.value = false;
          const data = response.data;
          if (data.success) {
            categorys = data.content;
            console.log("原始数组：", categorys);

            level1.value = [];
            level1.value = Tool.array2Tree(categorys, 0);
            console.log("树形结构：", level1.value);

            // 加载完分类后，再加载电子书，否则如果分类树加载很慢，则电子书渲染会报错
            handleQuery({
              page: 1,
              size: pagination.value.pageSize
            });
          } else {
            message.error(data.message);
          }
        });
      };

      const getCategoryName = (cid: number) => {
        // console.log(cid)
        let result = "";
        categorys.forEach((item: any) => {
          if (item.id === cid) {
            // return item.name; // 注意，这里直接return不起作用
            result = item.name;
          }
        });
        return result;
      };

      /**
       * 删除
       */
      const handleDelete = (id: number) => {
        axios.delete("/ebook/delete/" + id).then((response) => {
          const data = response.data; // data = commonResp
          if (data.success) {
            // 重新加载列表
            handleQuery({
              page: pagination.value.current,
              size: pagination.value.pageSize,
            });
          }
        });
      };

      onMounted(() => {
        handleQueryCategory();
      });


      return {
        param,
        ebooks,
        pagination,
        columns,
        loading,
        handleTableChange,
        handleQuery,
        level1,
        categoryIds,

        edit,
        add,
        handleDelete,


        ebook,
        modalVisible,
        modalLoading,
        handleModalOk,
        getCategoryName,


    }
  }
});
</script>

<style scoped>
  img {
  width: 50px;
  height: 50px;
}
</style>
