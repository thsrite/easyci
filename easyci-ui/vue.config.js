module.exports = {
    devServer: {
        proxy: {
            "/api": {
                target: "http://localhost:9875",
                changOrigin: true,
                pathRewrite: {"^/api" : ""}
              }
        }
      }
  }
