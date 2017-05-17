package org.yh.ble;


/**
 * 血氧
 */
@SuppressWarnings("serial")
public class FitSpoDataBean extends YhModel
{
	private String cmd;// 命令号
	private String pCode;// 产品编号
	private String batt;// 电量
	private String dataTime;// 生产日期
	private String sn; // 产品SN号
	private String sop;// 测量数值 百分比
	private String bpm;// 脉率数据 bpm
	private String blood_per;// 血流灌注 百分比
	private String hypoxaemic;// 血氧状态
	private String measuringTime;// 测量时间

	@Override
	public String toString()
	{
		String str = "";
		switch (Integer.parseInt(cmd))
		{
		case 0x00:
			str = "Cmd:00,TNo:" + pCode + ",Batt:" + batt + ",DATETIME:"
					+ dataTime + "sn:" + sn;
			break;
		case 0x01:
			str = "Cmd:01,TNo:" + pCode + ",Batt:" + batt;
			break;
		case 0x02:
			str = "Cmd:02,TNo:" + pCode + ",Batt:" + batt + ",sop:" + sop
					+ ",bpm:" + bpm + ",blood_per:" + blood_per
					+ ",hypoxaemic:" + hypoxaemic;
			break;
		case 0x03:
			str = "Cmd:03,TNo:" + pCode + ",Batt:" + batt + ",sop:" + sop
					+ ",bpm:" + bpm + ",blood_per:" + blood_per
					+ ",hypoxaemic:" + hypoxaemic;
			break;
		case 0x04:
			str = "Cmd:04,TNo:" + pCode + ",Batt:" + batt;
			break;
		}
		return str;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this.cmd.equals(((FitSpoDataBean) o).cmd))
		{
			return true;
		}
		return false;
	}

	public String getCmd()
	{
		return cmd;
	}

	public void setCmd(String cmd)
	{
		this.cmd = cmd;
	}

	public String getpCode()
	{
		return pCode;
	}

	public void setpCode(String pCode)
	{
		this.pCode = pCode;
	}

	public String getBatt()
	{
		return batt;
	}

	public void setBatt(String batt)
	{
		this.batt = batt;
	}

	public String getDataTime()
	{
		return dataTime;
	}

	public void setDataTime(String dataTime)
	{
		this.dataTime = dataTime;
	}

	public String getSn()
	{
		return sn;
	}

	public void setSn(String sn)
	{
		this.sn = sn;
	}

	public String getMeasuringTime()
	{
		return measuringTime;
	}

	public void setMeasuringTime(String measuringTime)
	{
		this.measuringTime = measuringTime;
	}

	public String getSop()
	{
		return sop;
	}

	public void setSop(String sop)
	{
		this.sop = sop;
	}

	public String getBpm()
	{
		return bpm;
	}

	public void setBpm(String bpm)
	{
		this.bpm = bpm;
	}

	public String getBlood_per()
	{
		return blood_per;
	}

	public void setBlood_per(String blood_per)
	{
		this.blood_per = blood_per;
	}

	public String getHypoxaemic()
	{
		return hypoxaemic;
	}

	public void setHypoxaemic(String hypoxaemic)
	{
		this.hypoxaemic = hypoxaemic;
	}
}
