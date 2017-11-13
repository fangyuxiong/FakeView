# Tools

## Layers Merge

减少层级工具：LayersMergeManager, LayersMergeEngine

### LayersMergeManager

将FrameLayout下所有子ViewGroup去掉，但不影响子View的显示效果。

使用方法：
```
//check if view tree need merge layers
if (!LayersMergeManager.needMerge(parent))
     return;
//all views must be layout before
LayersMergeManager manager = LayersMergeManager(parent, LayersMergeManager.EXTRACT_ALL);
manager.mergeChildrenLayers();
//done
```

### LayersMergeEngine

适合在list view（ListView、RecyclerView）中使用，通过LayersMergeManager减少View层级

可添加需要合并的图层，删除合并事件，暂停合并事件，继续合并事件等。

使用方法:
```
//添加需要合并的图层
LayersMergeEngine.getEngine().addMergeAction(tag, (FrameLayout) holder.itemView, LayersMergeManager.EXTRACT_ALL);
//删除合并事件
LayersMergeEngine.getEngine().removeMergeActionByTag(tag);
//暂停：
LayersMergeEngine.getEngine().pause();
//继续
LayersMergeEngine.getEngine().resume();
```