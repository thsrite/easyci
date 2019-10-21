import Vue from 'vue'
import App from './App.vue'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import axios from './assets/js/axios';
import Stomp from 'stompjs'
import SockJS from  'sockjs-client';
import router from './components/router'
import VueRouter from 'vue-router'
import { Loading } from 'element-ui';

Vue.use(VueRouter)
Vue.use(ElementUI)
Vue.config.productionTip = false
Vue.prototype.axios = axios
Vue.prototype.Stomp = Stomp
Vue.prototype.SockJS = SockJS
Vue.prototype.Loading = Loading

Vue.prototype.$message = ElementUI.Message

let loading
const startLoading = (text) => {  // 使用Element loading-start 方法
    loading = Loading.service({
        lock: true,
        text: text,
        background: 'rgba(0, 0, 0, 0.7)'
    });
    };
const endLoading = () =>{
    loading.close()
}
Vue.prototype.startLoading = startLoading
Vue.prototype.endLoading = endLoading

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
