<template>
    <div id="category-list">
        <div style="margin-top: 20px">
          <el-input style="width:200px;height:50px;padding-left: 20px;" v-model="searchContent" placeholder="输入商品分类名称搜索..."/>
          <el-divider direction="vertical"></el-divider>
          <el-button icon="el-icon-search" @click="getCategoryList" circle></el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="success" icon="el-icon-circle-plus" @click="openAdd" circle></el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="primary" icon="el-icon-edit"  @click="openEdit" circle></el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="danger" icon="el-icon-delete" @click="openRemove" circle></el-button>
        </div>
        <el-table
                ref="multipleTable"
                :data="tableData"
                tooltip-effect="dark"
                style="width: 100%"
                @selection-change="handleSelectionChange">
            <el-table-column
                    type="selection"
                    width="55">
            </el-table-column>
            <el-table-column
                    prop="id"
                    label="编号"
                    width="300">
            </el-table-column>
            <el-table-column
                    prop="name"
                    label="商品分类名称"
                    show-overflow-tooltip>
            </el-table-column>
        </el-table>
        <el-pagination
                background
                layout="prev, pager, next"
                :total="pageData.total"
                :page-size="pageData.size"
                :current-page="pageData.page"
                @current-change="changePage"
                style="padding-top:10px;">
        </el-pagination>
        <el-dialog :title="title" :visible.sync="dialogFormVisible">
            <el-form :model="form">
                <el-form-item label="商品分类名称" label-width="120px">
                    <el-input  v-model="form.name" autocomplete="off"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogFormVisible=false">取 消</el-button>
                <el-button type="primary" @click="saveCategory">确 定</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                tableData: [],
                multipleSelection: [],
                dialogFormVisible: false,
                form: {
                    id: "",
                    name: "",
                    sort: 0
                },
                title: "",
                searchContent: "",
                pageData: {
                    page: 1,
                    size: 5,
                    total: 0
                }
            }
        },
        methods: {
            handleSelectionChange(val) {
                this.multipleSelection = val;
            },
            openAdd(){
                this.form = {sort: 0};
                this.title = "添加商品分类信息";
                this.dialogFormVisible = true;
            },
            openEdit(){
                if(this.multipleSelection.length !== 1){
                    this.$message.warning("请选择一条数据进行修改！");
                    return false;
                }
                this.form = { ...this.multipleSelection[0] };
                this.title = "修改商品分类信息";
                this.dialogFormVisible = true;
            },
            openRemove(){
                if(this.multipleSelection.length !== 1){
                    this.$message.warning("请选择一条数据进行删除！");
                    return false;
                }
                this.form.id = this.multipleSelection[0].id;
                let _this = this;
                this.$confirm('确定要删除编号为：' + this.form.id + ' 的记录吗?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    _this.removeCategory();
                });
            },
            removeCategory(){
                let _this = this;
                this.$ajax.post(process.env.VUE_APP_SERVER + "/product/category/admin/remove", this.form).then((response)=>{
                    let resp = response.data;
                    if(resp.code === 0){
                        _this.$message.success(resp.msg);
                        _this.getCategoryList();
                    }else{
                        _this.$message.error(resp.msg);
                    }
                });
            },
            saveCategory(){
                let _this = this;
                this.$ajax.post(process.env.VUE_APP_SERVER + "/product/category/admin/save", this.form).then((response)=>{
                    let resp = response.data;
                    if(resp.code === 0){
                        _this.dialogFormVisible = false;
                        _this.$message.success(resp.msg);
                        _this.getCategoryList();
                    }else{
                        _this.$message.error(resp.msg);
                    }
                });
            },
            getCategoryList(){
                let _this = this;
                let data = {
                    searchContent: this.searchContent,
                    page: this.pageData.page
                };
                this.$ajax.post(process.env.VUE_APP_SERVER + "/product/category/admin/list", data).then((response)=>{
                    let resp = response.data;
                    if(resp.code === 0){
                        _this.tableData = resp.data.list;
                        _this.pageData.page = resp.data.page;
                        _this.pageData.size = resp.data.size;
                        _this.pageData.total = resp.data.total;
                    }
                });
            },
            changePage(currentPage){
                this.pageData.page = currentPage;
                this.getCategoryList();
            }
        },
        mounted(){
            this.getCategoryList();
        }
    }
</script>
