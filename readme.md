1.这是一个对windows系统下文件进行搜索的工具。
2.搜索方式：采用关键词或词组（多个关键词使用空格分割），针对文件的名称、路径、类型、（可读类型文件）内容进行搜索。
3.搜索不区分大小写，不指定关键词组的顺序，不限制特殊符号，可以在一段有效路径字符串或者可读类型文件的文本中任意截取字符串，都可以准确的搜索。
4.程序采用先索引后搜索的方式，首次使用时必须对系统下的文件进行扫描索引，这需要花费一定的时间；之后的索引操作将会根据缓存进行差异化更新，仅需
很少的时间。
5.程序支持对指定盘符下的文件（夹）修改（增加、删除、内容修改、重命名）进行实时监控、索引。
