# New Text
在`xfy.fakeview.library.text`包下

通过`ITextCompiler`生成`IDrawableBlockList`(其中包含`IdrawableBlock`)，并使用`TextDrawer`绘制文字，`TextDrawableDrawer`绘制图像。

整套逻辑已在`FTextDrwable`中，使用方法可参考`NewTextView`。

增加绘制文本的灵活性，优化文字中带有图片的解析速度与绘制速度。