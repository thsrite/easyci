<template>
  <div>
    <div style="width: 80%;margin: 0 auto">
      <div class="demo_container" >
        <el-select v-model="serverid" class="demo"  placeholder="请选择服务器" style="width: 10%;">
          <el-option
                  v-for="item in searchServerList"
                  :key="item.id"
                  :label="item.server_ip"
                  :value="item.id">
          </el-option>
        </el-select>
        <el-row class="button"><el-button @click="add()" >新增</el-button></el-row>
        <el-row class="button"><el-button @click="getOnePs()" >查看</el-button></el-row>
        <el-row class="button"><el-button @click="resetPs()" >重置</el-button></el-row>
        <el-row class="button"><el-button @click="autoFresh()" >{{isAutoFresh ? '关闭自动刷新' : '开启自动刷新'}}</el-button></el-row>
      </div>

      <div class="demo_container" >
        <el-select v-model="giturl" class="select"  filterable  placeholder="构建项目" value="">
          <el-option
                  v-for="item in gitlabPro"
                  :key="item.http_url_to_repo"
                  :label="item.name"
                  :value="item.http_url_to_repo">
            <span style="float: left">{{ item.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ item.http_url_to_repo }}</span>
          </el-option>
        </el-select>
        <el-input v-model="ports" class="demo" style="width: 10%" placeholder="端口5000:5000"></el-input>
        <el-select v-model="langu" class="select" placeholder="项目语言" value="">
          <el-option
                  v-for="item in langopt"
                  :key="item.label"
                  :label="item.label"
                  :value="item.label">
          </el-option>
        </el-select>
        <el-input v-model="maillist" class="demo" placeholder="请输入收件人邮箱"></el-input>
        <el-select v-model="deploy_way" class="select"  placeholder="部署方式" value="">
          <el-option
                  v-for="item in deployServerList"
                  :key="item.server_ip"
                  :label="item.server_ip"
                  :value="item.server_ip">
          </el-option>
        </el-select>
        <el-row  class="button">
          <el-button @click="deploy()" >部署</el-button>
        </el-row>

<!--        @close="killBuild()"-->
        <el-dialog title="部署日志"
                   :visible.sync="dialogVisible"
                   :close-on-click-modal="false"
                   style="padding: 50px;"
                    @close="reflashPage()">
          <!--        <div v-html="deployLog" id="log-container" style="text-align:left;height: 450px; overflow-y: scroll; background: #333; color: #aaa; padding: 50px;"></div>-->
          <el-input id="textarea_id"
                    type="textarea"
                    :rows="20"
                    placeholder="这是日志"
                    v-model="textarea"  readonly="">
          </el-input>
        </el-dialog>
        <el-dialog title="实时日志"
                   :visible.sync="dockerlogVisible"
                   :close-on-click-modal="false"
                   width="80%"
                   style="padding: 50px;"
                   @close="closeWebSocket(),closeSocket()">
          <div v-html="deployLog" id="log-container" style="text-align:left;height: 450px; overflow-y: scroll; background: #333; color: #aaa; padding: 50px;"></div>
        </el-dialog>

        <el-dialog
                title="添加服务器"
                :visible.sync="showInsertServe"
                :close-on-click-modal="false"
                width="40%" >
          <el-form :model="form">
            <el-form-item label="服务器IP地址：" :label-width="formLabelWidth">
              <el-input v-model="form.server" auto-complete="off"></el-input>
            </el-form-item>
            <el-form-item label="服务器端口：" :label-width="formLabelWidth">
              <el-input v-model="form.port" auto-complete="off"></el-input>
            </el-form-item>
            <el-form-item label="用户名：" :label-width="formLabelWidth">
              <el-input v-model="form.username" auto-complete="off"></el-input>
            </el-form-item>
            <el-form-item label="密码：" :label-width="formLabelWidth">
              <el-input v-model="form.password" auto-complete="off"></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addserver()">添加</el-button>
          </div>
        </el-dialog>

        <el-dialog title="设置系统配置"
                   :show-close="false"
                   :close-on-click-modal="false"
                   :close-on-press-escape="false"
                   :visible.sync="isLoginGitLabVisible"
                   width="80%"
                   style="padding: 50px;">
            <el-form ref="loginForm" :model="loginform" :rules="rules" label-width="80px" class="login-box">
              <el-form-item label="gitlab" prop="githost">
                <el-input type="text" placeholder="请输入gitlab地址" v-model="loginform.githost"/>
              </el-form-item>
              <el-form-item label="账号" prop="username">
                <el-input type="text" placeholder="请输入账号" v-model="loginform.username"/>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input type="password" placeholder="请输入密码" v-model="loginform.password"/>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" v-on:click="onSubmit('loginForm')">登录</el-button>
              </el-form-item>
            </el-form>

            <el-dialog
                    title="温馨提示"
                    :visible.sync="loginmessage"
                    width="30%">
              <span>请输入账号和密码、git地址</span>
              <span slot="footer" class="dialog-footer">
                <el-button type="primary" @click="loginmessage = false">确 定</el-button>
              </span>
            </el-dialog>
        </el-dialog>
      </div>

      <h4><p align="left">本机容器列表</p></h4>
      <el-table
                :data="localtableData"
                border>
        <el-table-column prop="container_id" label="容器id" width="115"></el-table-column>
        <el-table-column prop="image_name" label="镜像名称" width="335"></el-table-column>
        <el-table-column label="状态" width="50">
          <template slot-scope="scope">
            <img v-if="scope.row.status === 'Up'" :src="require('../../assets/static/up.png')" width="30" height="30" class="head_pic"/>
            <img v-else-if="scope.row.status === 'Exited'" :src="require('../../assets/static/down.png')" width="30" height="30" class="head_pic"/>
          </template>
        </el-table-column>
        <el-table-column prop="created" label="创建时间" width="120"></el-table-column>
        <el-table-column prop="ports" label="端口映射" width="320"></el-table-column>
        <el-table-column prop="container_name" label="容器名称" width="155"></el-table-column>
        <el-table-column prop="server_ip" label="服务器" width="110"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button @click="startDocker(scope.row)" type="text" size="small">启动</el-button>
            <el-button @click="stopDocker(scope.row)" type="text" size="small">停止</el-button>
            <el-button @click="restartDocker(scope.row)" type="text" size="small">重启</el-button>
            <el-button @click="destroyDocker(scope.row)" type="text" size="small">销毁</el-button>
          </template>
        </el-table-column>
      </el-table>
      <h4><p align="left">服务器容器列表</p></h4>
      <el-table
              :data="tableData"
              border>
        <el-table-column prop="container_id" label="容器id" width="115"></el-table-column>
        <el-table-column prop="image_name" label="镜像名称" width="335"></el-table-column>
        <el-table-column label="状态" width="50">
          <template slot-scope="scope">
            <img v-if="scope.row.status === 'Up'" :src="require('../../assets/static/up.png')" width="30" height="30" class="head_pic"/>
            <img v-else-if="scope.row.status === 'Exited'" :src="require('../../assets/static/down.png')" width="30" height="30" class="head_pic"/>
          </template>
        </el-table-column>
        <el-table-column prop="created" label="创建时间" width="120"></el-table-column>
        <el-table-column prop="ports" label="端口映射" width="320"></el-table-column>
        <el-table-column prop="container_name" label="容器名称" width="155"></el-table-column>
        <el-table-column prop="server_ip" label="服务器" width="110"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button @click="startDocker(scope.row)" type="text" size="small">启动</el-button>
            <el-button @click="stopDocker(scope.row)" type="text" size="small">停止</el-button>
            <el-button @click="restartDocker(scope.row)" type="text" size="small">重启</el-button>
            <el-button @click="destroyDocker(scope.row)" type="text" size="small">销毁</el-button>
            <el-button @click="dockerLogs(scope.row)" type="text" size="small">日志</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'easyci',
    props: {
      msg: String
    },
    data () {
      return {
        params:{
          username:'',
          password:''
        },
        dialogTableVisible: false,
        tableData: [],
        localtableData: [],
        langopt: [{
          label: 'java'
        }, {
          label: 'vue'
        }],
        langu: '',
        serverid: '',
        deployopt: [],
        gitlabPro: [],
        deploy_way: '',
        searchServerList: [],
        deployServerList: [],
        showInsertServe:false,
        form: {
          server: '',
          port: '',
          username: '',
          password: '',
          islocal: 0,
        },
        giturl: '',
        ports: '',
        maillist: '',
        textarea: '',
        start: 0,
        status:true,
        dialogVisible:false,
        websocket: null,
        stompClient: null,
        deployLog: '',
        deploying_project:'',
        dockerlogVisible:false,
        img: '',
        isDockerPsOne: false,
        isReSet:false,
        isAutoFresh:false,
        isLoginGitLabVisible:false,
        formLabelWidth: '120px',
        freshnum:0,
        container_name: '',
        server_ip:'',
        loginmessage:false,
        loginform: {
          username: '',
          password: '',
          githost: ''
        },
        random:'',
        rules: {
          githost: [
            {required: true, message: '地址不可为空', trigger: 'blur'}
          ],
          username: [
            {required: true, message: '账号不可为空', trigger: 'blur'}
          ],
          password: [
            {required: true, message: '密码不可为空', trigger: 'blur'}
          ]
        }
      }
    },
    mounted() {
      // //连接发生错误的回调方法
      // this.websocket.onerror = function(){
      //   console.log("error");
      // };
      //
      // //连接成功建立的回调方法
      // this.websocket.onopen = function(event){
      //   console.log("open");
      // }
      //
      // //接收到消息的回调方法
      // this.websocket.onmessage = function(event){
      //   console.log(event.data);
      // }
      //
      // //连接关闭的回调方法
      // this.websocket.onclose = function(){
      //   let that = this
      //   that.textarea = "close";
      // }
      //
      //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
      window.onbeforeunload = function() {
        let that = this
        that.websocket.close();
        that.stompClient.disconnect();
        that.websocket = null
        that.stompClient = null
      }

      // this.getSearchServerList();
      // this.getDeployServerList();
      // // this.getProjects();
      // this.getDockerPs();
      // this.getLocalPs();
      this.IsSetSystemSetting()
    },
    methods:{
      getProjects(){
        let that = this
        this.axios.post('/api/gitlab/projects', {
        }).then(function (response) {
          if (response.data.status){
            that.gitlabPro = response.data.list
          } else {
            that.$message.error(response.data.errorDesc);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
      },
      onSubmit(formName) {
        let that = this
        this.startLoading("验证中…")
        this.axios.post('/api/gitlab/login', {
          username:that.loginform.username,
          password:that.loginform.password,
          githost:that.loginform.githost
        }).then(function (response) {
          that.endLoading()
          if (response.data.status){
            that.$message.success(response.data.errorDesc);
            that.isLoginGitLabVisible = false
            that.getSearchServerList();
            that.getDeployServerList();
            that.getProjects();
            that.getDockerPs();
            that.getLocalPs();
          }else{
            that.$message.error(response.data.errorDesc);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
      },
      IsSetSystemSetting(){
        let that = this
        this.axios.post('/api/gitlab/isSet', {
        }).then(function (response) {
          if (response.data.status){
            that.isLoginGitLabVisible = false
            that.getSearchServerList();
              that.getDeployServerList();
            that.getProjects();
            that.getDockerPs();
            that.getLocalPs();
          }else{
            that.isLoginGitLabVisible = true
            that.$message.error(response.data.errorDesc);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
      },
      getDockerPs(){
        let that = this
        this.axios.post('/api/docker/allps', {
        }).then(function (response) {
          // that.$message(response.data.errorDesc);
          if (response.data.status){
            if (response.data.list.status === "Up"){
              that.img = require('../../assets/static/up.png')
            }else{
              that.img = require('../../assets/static/down.png')
            }
            that.tableData = response.data.list;
          } else {
            that.showInsertServe = true
            that.$message.error("请添加服务器")
          }
        })
        .catch(function (error) {
          console.log(error);
        });
      },
      getSearchServerList(){
        let that = this
        this.axios.get('/api/server/searchServerList', {
        }).then(function (response) {
          that.searchServerList = response.data.list
        })
        .catch(function (error) {
          console.log(error);
        });
      },
      getDeployServerList(){
        let that = this
        this.axios.get('/api/server/deployServerList', {
        }).then(function (response) {
          that.deployServerList = response.data.list
        })
        .catch(function (error) {
          console.log(error);
        });
      },
      autoFresh(){
        let that = this
        that.freshnum = 0
        that.isAutoFresh = ! that.isAutoFresh
        var a=setInterval(()=>{
          if (that.isAutoFresh){
            if (that.freshnum === 0) {
              that.$message("开启自动刷新")
            }
            if (! that.isReSet && that.isDockerPsOne){
              if (that.serverid != null && that.serverid !== '') {
                this.getOnePs();
              }
              this.getLocalPs();
              that.freshnum += 1
            } else {
              that.freshnum += 1
              this.getDockerPs();
              this.getLocalPs();
            }
          }else{
            clearInterval(a)
            that.$message("关闭自动刷新")
          }
        }, 1000);
      },
      resetPs(){
        let that = this
        that.serverid = ''
        that.isReSet = true
        this.getDockerPs();
        this.getLocalPs();
      },
      getLocalPs(){
        let that = this
        this.axios.post('/api/docker/localps', {
        }).then(function (response) {
          // that.$message(response.data.errorDesc);
          if (response.data.list.status === "Up"){
            that.img = require('../../assets/static/up.png')
          }else{
            that.img = require('../../assets/static/down.png')
          }
          that.localtableData = response.data.list;
        })
        .catch(function (error) {
          console.log(error);
        });
      },
      getOnePs(){
        let that = this
        if (this.serverid == null || this.serverid === '') {
          that.$message.warning("请选择服务器ip")
          return false;
        }
        this.axios.post('/api/docker/oneps', {
          serverid:this.serverid,
        },).then(function (response) {
          // that.$message(response.data.errorDesc);
          that.tableData = response.data.list
          that.isDockerPsOne = response.data.status
          that.isReSet = ! response.data.status
        })
        .catch(function (error) {
          console.log(error);
        });
      },
      add(){
        this.showInsertServe = ! this.showInsertServe
      },
      addserver(){
        let that = this
        this.startLoading("认证中…")
        this.axios.post('/api/server/add', {
          server_ip:this.form.server,
          server_port:this.form.port,
          server_username:this.form.username,
          server_password:this.form.password,
          is_local:this.form.islocal
        },).then(function (response) {
          that.endLoading()
          if (response.data.status){
            that.$message.success(response.data.errorDesc);
            that.showInsertServe = false
            that.reflashPage()
          }else {
            that.$message.error(response.data.errorDesc);
          }
        })
      },
      deploy() {
        let that = this
        that.status = true
        that.dialogVisible = true
        that.textarea = ''
        setTimeout(function() {
          that.logs()
        }, 2000)
        this.axios.post('/api/docker/build', {
          giturl: this.giturl,
          ports: this.ports,
          language: this.langu,
          mails: this.maillist,
          deploy_way: this.deploy_way
        }).then(function (response) {
          that.status = response.data.status
          if (!response.data.status) {
            that.dialogVisible = response.data.status
            that.$message.error(response.data.errorDesc);
          }else {
            that.$message.success(response.data.errorDesc);
            if (that.isDockerPsOne){
              if (that.serverid != null && that.serverid !== '') {
                that.getOnePs();
              }
              that.getLocalPs();
            } else {
              that.getDockerPs();
              that.getLocalPs();
            }
          }
        })
      },
      logs(){
        this.start = 0
        this.deployLogs()
      },
      deployLogs(){
        let that = this
        this.axios.post('/api/docker/deployLogs', {
          giturl: this.giturl,
          deploy_way: this.deploy_way,
          start: this.start
        },).then(function (response) {
          if (response.data.list.log === "…") {
            that.textarea = ""
            that.deployLogs()
          }else {
            that.textarea += response.data.list.log
            that.start = response.data.list.start
            if (response.data.list.endsign  !==0){
              that.deployLogs()
              const textarea1 = document.getElementById('textarea_id');
              textarea1.scrollTop = textarea1.scrollHeight;
            }
          }
        })
      },
      dockerLogs(row){
        let that = this
        that.dockerlogVisible = true
        that.deployLog = ''
        that.random = Math.floor(Math.random()*20)
        that.container_name = row.container_name
        that.server_ip = row.server_ip
        // this.axios.post('/api/docker/dockerLogs', {
        //   container_name: row.container_name,
        //   server_ip: row.server_ip
        // },).then(function (response) {
        //   // that.textarea += response.data.list.log
        //   // that.start = response.data.list.start
        //   // const textarea1 = document.getElementById('textarea_id');
        //   // textarea1.scrollTop = textarea1.scrollHeight;
        //   if (! that.dockerlogVisible) {
        //     return
        //   }
        // })
        let name = row.container_name + "|" + that.random + "|" + row.server_ip
        if('WebSocket' in window){
          this.websocket = new WebSocket("ws://" + location.hostname + ":9875/websocket/" + name);
        }else{
          alert('Not support websocket')
        }
        if (that.dockerlogVisible){
          that.openSocket()
        }else {
          that.closeWebSocket()
          that.closeSocket()
        }
      },
      killDockerLogs(row){
        let that = this
        this.axios.post('/api/docker/killLogs', {
          container_name: that.container_name,
          server_ip: that.server_ip
        },).then(function (response) {
          if(response.data.status){
            that.$message("关闭实时日志")
          }
        })
      },
      killBuild(){
        let that = this
        this.axios.post('/api/docker/killbuild', {
          giturl: that.giturl
        },).then(function (response) {
          if(response.data.status){
            that.$message("关闭构建")
            that.status = false
          }
        })
      },
      startDocker(row){
        let that = this
        this.startLoading("启动中…")
        this.axios.post('/api/docker/dockerExec', {
          container_name: row.container_name,
          server_ip: row.server_ip,
          cmd: 'start'
        },).then(function (response) {
          that.endLoading()
          if (response.data.status){
            that.$message.success(response.data.errorDesc);
            if (that.isDockerPsOne){
              if (that.serverid != null && that.serverid !== '') {
                that.getOnePs();
              }
              that.getLocalPs();
            } else {
              that.getDockerPs();
              that.getLocalPs();
            }
          } else {
            that.$message.error(response.data.errorDesc);
          }
        })
      },
      stopDocker(row){
        let that = this
        this.startLoading("停止中…")
        this.axios.post('/api/docker/dockerExec', {
          container_name: row.container_name,
          server_ip: row.server_ip,
          cmd: 'stop'
        },).then(function (response) {
          that.endLoading()
          if (response.data.status){
            that.$message.success(response.data.errorDesc);
            if (that.isDockerPsOne){
              if (that.serverid != null && that.serverid !== '') {
                that.getOnePs();
              }
              that.getLocalPs();
            } else {
              that.getDockerPs();
              that.getLocalPs();
            }
          } else {
            that.$message.error(response.data.errorDesc);
          }
        })
      },
      restartDocker(row){
        let that = this
        this.startLoading("重启中…")
        this.axios.post('/api/docker/dockerExec', {
          container_name: row.container_name,
          server_ip: row.server_ip,
          cmd: 'restart'
        },).then(function (response) {
          that.endLoading()
          if (response.data.status){
            that.$message.success(response.data.errorDesc);
            if (that.isDockerPsOne){
              if (that.serverid != null && that.serverid !== '') {
                that.getOnePs();
              }
              that.getLocalPs();
            } else {
              that.getDockerPs();
              that.getLocalPs();
            }
          } else {
            that.$message.error(response.data.errorDesc);
          }        })
      },
      destroyDocker(row){
        let that = this
        this.startLoading("销毁中…")
        if (row.status === "Up"){
          that.$message.warning("请先停止容器！")
          that.endLoading()
          return false;
        }else {
          this.axios.post('/api/docker/dockerExec', {
            container_name: row.container_name,
            server_ip: row.server_ip,
            cmd: 'rm'
          },).then(function (response) {
            that.endLoading()
            if (response.data.status){
              that.$message.success(response.data.errorDesc);
              if (that.isDockerPsOne){
                if (that.serverid != null && that.serverid !== '') {
                  that.getOnePs();
                }
                that.getLocalPs();
              } else {
                that.getDockerPs();
                that.getLocalPs();
              }
            } else {
              that.$message.error(response.data.errorDesc);
            }        })
        }
      },
      // sendmessage(){
      //   let that = this
      //   that.postValue.id=1;
      //   that.postValue.message=1;
      //   this.websocket.send(JSON.stringify(that.postValue));
      // },
      //关闭连接
      closeWebSocket(){
        let that = this
        if(this.websocket != null) {
          this.websocket.close();
        }
        that.dockerlogVisible = false
      },
      openSocket() {
        let that = this
        if(this.stompClient==null){
          let socket = new this.SockJS('http://' +  location.hostname + ':9875/websocket');
          this.stompClient = this.Stomp.over(socket);
          this.stompClient.connect({},
          function() {
            that.stompClient.subscribe('/topic/' + that.container_name + that.random, function(event) {
              // let content=JSON.parse(event.body);
              // console.log(content)
              that.deployLog += (event.body + "</br>")
              const textarea1 = document.getElementById('log-container');
              textarea1.scrollTop = textarea1.scrollHeight;
            },{
              token:"kltoen"
            });
          });
        }
      },
      closeSocket() {
        let that = this
        if (this.stompClient != null) {
          this.stompClient.disconnect();
          this.stompClient=null;
          that.dockerlogVisible = false
        }
       },
      reflashPage(){
        let NewPage = '_empty' + '?time=' + new Date().getTime()/500;
        this.$router.push(NewPage);
        this.$router.go(-1);
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .demo_container:after {
    content: "";
    clear: both;
    display: block;
    width: 100%;
    margin: 0 auto;
    marign-bottom: 20px;
  }
  .demo{
    width: 20%;
    box-sizing: border-box;
    float: left;
  }
  .server{
    width: 40%;
    box-sizing: border-box;
    float: right;
  }
  .select{
    width: 10%;
    box-sizing: border-box;
    float: left;
  }
  .button{
    width: 5%;
    box-sizing: border-box;
    float: left;
  }
  .buttonr{
    width: 5%;
    box-sizing: border-box;
    float: right;
  }
  h3 {
    margin: 40px 0 0;
  }
  ul {
    list-style-type: none;
    padding: 0;
  }
  li {
    /*display: inline-block;*/
    margin: 0 10px;
  }
  a {
    color: #42b983;
  }
</style>
