import Router from 'vue-router'


const router = new Router({
    routes: [
        // {
        //     path: '/',
        //     name: 'Login',
        //     component: () => import('../views/Login')//路由懒加载方式
        // },
        {
            path: '/',
            name: 'Easyci',
            component: () => import('../views/Easyci')
        }
    ]
})
export default router
