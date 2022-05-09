package emu.grasscutter.data.def;

import emu.grasscutter.data.GameResource;
import emu.grasscutter.data.ResourceType;
import emu.grasscutter.data.ResourceType.LoadPriority;
import emu.grasscutter.data.common.ItemParamData;
import emu.grasscutter.data.common.ItemParamStringData;

import java.util.Arrays;

@ResourceType(name = "RewardPreviewExcelConfigData.json", loadPriority = LoadPriority.HIGH)
public class RewardPreviewData extends GameResource {
	private int Id;
	private String Desc;
	private ItemParamStringData[] PreviewItems;
	private ItemParamData[] PreviewItemsArray;

	@Override
	public int getId() {
		return this.Id;
	}

	public ItemParamData[] getPreviewItems() {
		return PreviewItemsArray;
	}

	public String getDesc() {
		return Desc;
	}

	@Override
	public void onLoad() {
		if (this.PreviewItems != null && this.PreviewItems.length > 0) {
			this.PreviewItemsArray = Arrays.stream(this.PreviewItems)
					.filter(d -> d.getId() > 0 && d.getCount() != null && !d.getCount().isEmpty())
					.map(ItemParamStringData::toItemParamData)
					.toArray(size -> new ItemParamData[size]);
		} else {
			this.PreviewItemsArray = new ItemParamData[0];
		}
	}
}
