const path = require('path');

module.exports = {
  //webpack take takes this file:
  entry: './foreign-libs/index.js',
  output: {
    //and creates this file:
    filename: 'index.bundle.js',
    //in this dir:
    path:  path.resolve(__dirname, 'foreign-libs')
  },
  // optimization: {
  //   usedExports: true
  // }
}
