# NewTextView
一个新的TextView继承自View，主要实现类为FTextDrawable，减少设置文本耗时，内存占用。建议在列表中使用。

经测试，将消息列表中文本消息改为NewTextView后，平均设置文本耗时为之前的1/10，LayoutTextView设置文本耗时在0.7ms到1.3ms之间，NewTextView设置文本耗时在0.08ms到0.09ms，偶尔有0.1ms或1ms耗时。
# 不足
不可设置文本上下左右的Drawable(`TextView#setCompoundDrawables`)，不可设置省略号位置，默认在最后（TextView可设置在开头，中部或结尾）。
# 使用方法
解析文字需要用到`ITextCompiler`，目前已有多个实现： `DefaultTextCompiler`（纯文本），`DrawableTextCompiler`（带表情），`ClickSpanTextCompiler`（可解析带有`BaseSpan`的`Spanned`文本），`SpecialCompiler`（解析富文本，目前主要用在weex中，如`"(font color='xxx' size='xxx' background='xxx' weight='xxx')(/font)"`) ； 其中`DefaultTextCompiler`是基类，且每个类中都有一个单例对象。

若文本中没有`BaseSpan`或不使用`ClickSpanTextCompiler`，可直接设置原始文本，如textview.setText("测试[/表情]测试")；若其中文本可点击，或样式要改变，则需要将整个文本转换成`Spanned`，并将特殊文本用`BaseSpan`(`FClickableSpan`可点击)包裹。
### DrawableTextCompiler
需要设置`DrawableTextCompiler.ResourceAdapter`

`MomoEmotionUtil.getEmoteStaticCompiler()` 可解析静态表情

`MomoEmotionUtil.getEmoteDynamicCompiler()` 可解析静态表情和动态表情
### 自定义Compiler
任何一个Compiler都可以设置一个`innerCompiler`（装饰器模式，参考`InputStream`），最常用为`ClickSpanTextCompiler compiler = new ClicSpanTextCompiler(MomoEmotionUtil.getEmoteDynamicCompiler())` ，这段代码可以在`NewTextViewHelper`看到
## 注意
compiler最好能复用，将一些特殊的compiler使用静态变量保存起来。
# xml中的style支持
```
android:textSize
android:maxLines
android:textColor
android:text
android:gravity
android:maxWidth 可填负值，如-10dp，表示屏幕宽-10dp
android:maxHeight 可填负值，如-10dp，表示屏幕高-10dp
android:lineSpacingExtra 行间距，默认0，可设置负值或正值

fntv_style:全局style
fntv_drawable_size: 文字中图片或表情的大小 dimension
fntv_drawable_scale: 文字中图片或表情相对textsize的缩放比 float
fntv_measure_when_set_text: 设置文字时是否立刻计算文字长宽，一般不使用，若设置为true，设置文字可能会触发两次requestLayout boolean
fntv_ellepsize_text: 可设置末尾省略号文本，默认为... string
fntv_text_compiler: 默认compiler，可选(text_only|contain_image|click_span|spcial_text)，或自定义compiler的全类名，注意：这里获取的都是compiler中的单例，所以自定义的compiler一定要有 getCompiler()方法
```
# 实现原理
通过`ITextCompiler`将一段文本(`CharSequence`)转化为`IDrawableBlockList`（继承`IBlock`），这其中包含多个`IDrawableBlock`（继承`IBlock`）。block类型有6种：纯文本，图片类型3种（见下方），span类型（其中包含一个`IDrawableBlockList`）和换行符类型。在给定最大宽高后，通过`measure`方法计算每个block占用长宽和行数，并合在一起计算出总占用长宽和行数。`draw`方法中通过长宽和行数绘制每一个block。

每个Compiler中都有一个缓存表，key为文本，value为解析出的`IDrawableBlockList`，默认长度为30，所以当在列表中使用时，解析文本耗时很少，若其中有静态图片，则图片实例为同一个，减少内存占用。
# 重要类
`FTextDrawable`承载文本解析，计算及绘制工作
`IDrawableBlockList`文本块集合
`IDrawableBlock`文本块
`ITextCompiler`将文本解析为文本块
`MeasureTextUtils`计算文本长宽行数主要工具
`TextDrawer`绘制文本主要工具
`TextDrawableDrawer`绘制文本中的图片主要工具
`BaseSpan`可设置粗体、下划线、斜体、文字颜色、背景颜色
`FClickableSpan`继承自`BaseSpan`，可点击
# 三种图片类型
参考`DrawableTextCompiler.ResourceAdapter`接口
- 给定一个drawable id，由`DrawableTextCompiler.ResourceAdapter`接口中的`parseRes`返回
- 给定一个特殊drawable，由`DrawableTextCompiler.ResourceAdapter`接口中的`parseDrawable`返回
- 给定一个实现了`IDrawableStats`的drawable，会设置`Drawable.Callback`，由`DrawableTextCompiler.ResourceAdapter`接口中的`parseDrawable`返回，且实现`IDrawableStats`
前两种drawable都可复用，第三种主要用在动态图片中，参考`ApngDrawable`。
