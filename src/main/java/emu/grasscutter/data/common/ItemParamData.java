package emu.grasscutter.data.common;

public class ItemParamData {
	private int Id;
    private int Count;

    public ItemParamData() {}
	public ItemParamData(int id, int count) {
    	this.Id = id;
		this.Count = count;
	}

	public int getId() {
		return Id;
	}

	public int getCount() {
		return Count;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ItemParamData{");
		sb.append("Id=").append(Id);
		sb.append(", Count=").append(Count);
		sb.append('}');
		return sb.toString();
	}
}
