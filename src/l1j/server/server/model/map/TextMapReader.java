/**
 *                            License
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF THIS  
 * CREATIVE COMMONS PUBLIC LICENSE ("CCPL" OR "LICENSE"). 
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.  
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR  
 * COPYRIGHT LAW IS PROHIBITED.
 * 
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND  
 * AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. TO THE EXTENT THIS LICENSE  
 * MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU THE RIGHTS CONTAINED 
 * HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 * 
 */
package l1j.server.server.model.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.datatables.MapsTable;
import l1j.server.server.utils.FileUtil;
import l1j.server.server.utils.collections.Lists;
import l1j.server.server.utils.collections.Maps;

/**
 * 地圖 (maps/\d*.txt)讀取
 */
public class TextMapReader extends MapReader {

	/** 紀錄用 */
	private static Logger _log = Logger
			.getLogger(TextMapReader.class.getName());

	/** 地圖的路徑 */
	private static final String MAP_DIR = "./maps/";

	/** MAP_INFO 中編號的位置 */
	public static final int MAPINFO_MAP_NO = 0;

	/** MAP_INFO 開始X座標的位置 */
	public static final int MAPINFO_START_X = 1;

	/** MAP_INFO 結束X座標的位置 */
	public static final int MAPINFO_END_X = 2;

	/** MAP_INFO 開始Y座標的位置 */
	public static final int MAPINFO_START_Y = 3;

	/** MAP_INFO 結束Y座標的位置 */
	public static final int MAPINFO_END_Y = 4;

	/**
	 * 依照輸入大小讀取指定編號地圖
	 * 
	 * @param mapId
	 *            地圖編號
	 * @param xSize
	 *            X座標大小
	 * @param ySize
	 *            Y座標大小
	 * @return byte[][]
	 * @throws IOException
	 */
	public byte[][] read(final int mapId, final int xSize, final int ySize)
			throws IOException {
		byte[][] map = new byte[xSize][ySize];
		LineNumberReader in = new LineNumberReader(new BufferedReader(
				new FileReader(MAP_DIR + mapId + ".txt")));

		int y = 0;
		String line;
		while ((line = in.readLine()) != null) {
			if ((line.trim().length() == 0) || line.startsWith("#")) {
				continue; // 跳過空行與註解
			}

			int x = 0;
			StringTokenizer tok = new StringTokenizer(line, ",");
			while (tok.hasMoreTokens()) {
				byte tile = Byte.parseByte(tok.nextToken());
				map[x][y] = tile;

				x++;
			}
			y++;
		}
		in.close();
		return map;
	}

	/**
	 * 讀取指定編號的地圖
	 * 
	 * @param id
	 *            地圖編號
	 * @return L1Map
	 * @throws IOException
	 */
	@Override
	public L1Map read(final int id) throws IOException {
		for (int[] info : MAP_INFO) {
			int mapId = info[MAPINFO_MAP_NO];
			int xSize = info[MAPINFO_END_X] - info[MAPINFO_START_X] + 1;
			int ySize = info[MAPINFO_END_Y] - info[MAPINFO_START_Y] + 1;

			if (mapId == id) {
				L1V1Map map = new L1V1Map((short) mapId, this.read(mapId,
						xSize, ySize), info[MAPINFO_START_X],
						info[MAPINFO_START_Y], MapsTable.getInstance()
								.isUnderwater(mapId), MapsTable.getInstance()
								.isMarkable(mapId), MapsTable.getInstance()
								.isTeleportable(mapId), MapsTable.getInstance()
								.isEscapable(mapId), MapsTable.getInstance()
								.isUseResurrection(mapId), MapsTable
								.getInstance().isUsePainwand(mapId), MapsTable
								.getInstance().isEnabledDeathPenalty(mapId),
						MapsTable.getInstance().isTakePets(mapId), MapsTable
								.getInstance().isRecallPets(mapId), MapsTable
								.getInstance().isUsableItem(mapId), MapsTable
								.getInstance().isUsableSkill(mapId));
				return map;
			}
		}
		throw new FileNotFoundException("地圖編號: " + id);
	}

	/**
	 * 取得所有地圖與編號的 Mapping
	 * 
	 * @return Map
	 * @throws IOException
	 */
	@Override
	public Map<Integer, L1Map> read() throws IOException {
		Map<Integer, L1Map> maps = Maps.newMap();

		for (int[] info : MAP_INFO) {
			int mapId = info[MAPINFO_MAP_NO];
			int xSize = info[MAPINFO_END_X] - info[MAPINFO_START_X] + 1;
			int ySize = info[MAPINFO_END_Y] - info[MAPINFO_START_Y] + 1;

			try {
				L1V1Map map = new L1V1Map((short) mapId, this.read(mapId,
						xSize, ySize), info[MAPINFO_START_X],
						info[MAPINFO_START_Y], MapsTable.getInstance()
								.isUnderwater(mapId), MapsTable.getInstance()
								.isMarkable(mapId), MapsTable.getInstance()
								.isTeleportable(mapId), MapsTable.getInstance()
								.isEscapable(mapId), MapsTable.getInstance()
								.isUseResurrection(mapId), MapsTable
								.getInstance().isUsePainwand(mapId), MapsTable
								.getInstance().isEnabledDeathPenalty(mapId),
						MapsTable.getInstance().isTakePets(mapId), MapsTable
								.getInstance().isRecallPets(mapId), MapsTable
								.getInstance().isUsableItem(mapId), MapsTable
								.getInstance().isUsableSkill(mapId));

				maps.put(mapId, map);
			} catch (IOException e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}

		return maps;
	}

	/**
	 * 傳回所有地圖的編號
	 * 
	 * @return ArraryList
	 */
	public static List<Integer> listMapIds() {
		List<Integer> ids = Lists.newList();

		File mapDir = new File(MAP_DIR);
		for (String name : mapDir.list()) {
			File mapFile = new File(mapDir, name);
			if (!mapFile.exists()) {
				continue;
			}
			if (!FileUtil.getExtension(mapFile).toLowerCase().equals("txt")) {
				continue;
			}
			int id = 0;
			try {
				String idStr = FileUtil.getNameWithoutExtension(mapFile);
				id = Integer.parseInt(idStr);
			} catch (NumberFormatException e) {
				continue;
			}
			ids.add(id);
		}
		return ids;
	}

	/**
	 * mapInfo：地圖編號、地圖構成大小. 格式為 {mapNo,StartX,EndX,StartY,EndY}
	 * 地圖編號與名稱, 請自行參照用戶端 Text.idx與 Text.pak 裡包的zone*.tbl等檔案
	 */
	private static final int[][] MAP_INFO = {
		{ 0, 32256, 32767, 32768, 33279 },
		{ 1, 32576, 32831, 32704, 32959 },
		{ 2, 32576, 32895, 32704, 32959 },
		{ 3, 32576, 32831, 32704, 32959 },
		{ 4, 32448, 34303, 32064, 33599 },
		{ 5, 32704, 32767, 32768, 32831 },
		{ 6, 32704, 32831, 32768, 32831 },
		{ 7, 32640, 32895, 32640, 32895 },
		{ 8, 32640, 32895, 32640, 32895 },
		{ 9, 32640, 32895, 32640, 32895 },
		{ 10, 32640, 32831, 32640, 32895 },
		{ 11, 32640, 32895, 32640, 32895 },
		{ 12, 32640, 32895, 32640, 32895 },
		{ 13, 32640, 32895, 32640, 32895 },
		{ 14, 32512, 33023, 32704, 32895 },
		{ 15, 32704, 32767, 32768, 32831 },
		{ 16, 32704, 32831, 32704, 32895 },
		{ 17, 32704, 32767, 32768, 32831 },
		{ 18, 32704, 32767, 32768, 32831 },
		{ 19, 32704, 32895, 32640, 32895 },
		{ 20, 32704, 32831, 32704, 32831 },
		{ 21, 32640, 32831, 32704, 32895 },
		{ 22, 32640, 32895, 32640, 32895 },
		{ 23, 32640, 32895, 32640, 32895 },
		{ 24, 32640, 32895, 32640, 32895 },
		{ 25, 32704, 32895, 32704, 32831 },
		{ 26, 32704, 32895, 32640, 32895 },
		{ 27, 32640, 32895, 32704, 32895 },
		{ 28, 32640, 32895, 32704, 32895 },
		{ 29, 32704, 32767, 32768, 32831 },
		{ 30, 32640, 32895, 32640, 32895 },
		{ 31, 32640, 32895, 32640, 32895 },
		{ 32, 32576, 32831, 32704, 32959 },
		{ 33, 32576, 32831, 32704, 32959 },
		{ 34, 32640, 32831, 32704, 32895 },
		{ 35, 32576, 32831, 32704, 32959 },
		{ 36, 32576, 32831, 32704, 32959 },
		{ 37, 32576, 32831, 32704, 32959 },
		{ 38, 32640, 32831, 32704, 32895 },
		{ 39, 32640, 32831, 32704, 32895 },
		{ 40, 32640, 32831, 32704, 32895 },
		{ 41, 32640, 32831, 32704, 32895 },
		{ 42, 32512, 32831, 32704, 33023 },
		{ 43, 32704, 32831, 32704, 32831 },
		{ 44, 32704, 32831, 32704, 32831 },
		{ 45, 32704, 32831, 32704, 32831 },
		{ 46, 32704, 32831, 32704, 32831 },
		{ 47, 32704, 32831, 32704, 32831 },
		{ 48, 32704, 32831, 32704, 32831 },
		{ 49, 32704, 32831, 32704, 32831 },
		{ 50, 32704, 32831, 32704, 32831 },
		{ 51, 32640, 32831, 32704, 32895 },
		{ 52, 32640, 32767, 32768, 32895 },
		{ 53, 32640, 32895, 32640, 32895 },	
		{ 54, 32640, 32895, 32640, 32895 },	
		{ 55, 32640, 32831, 32704, 32959 },	
		{ 56, 32640, 32895, 32640, 32895 },	
		{ 57, 32576, 33023, 32512, 32959 },
		{ 58, 32512, 32831, 32704, 33023 },
		{ 59, 32640, 32831, 32768, 32895 },
		{ 60, 32640, 32767, 32768, 32959 },
		{ 61, 32640, 32767, 32768, 32895 },
		{ 62, 32640, 32767, 32768, 32895 },
		{ 63, 32576, 32895, 32640, 32959 },
		{ 64, 32512, 32639, 32768, 32895 },
		{ 65, 32704, 32831, 32768, 32895 },
		{ 66, 32704, 32895, 32768, 32959 },
		{ 67, 32640, 32831, 32704, 32895 },
		{ 68, 32576, 33023, 32512, 32959 },
		{ 69, 32512, 32831, 32704, 33023 },
		{ 70, 32576, 33023, 32640, 33087 },
		{ 72, 32704, 32895, 32768, 32895 },
		{ 73, 32704, 32959, 32704, 32959 },
		{ 74, 32704, 32895, 32768, 32959 },
		{ 75, 32704, 32831, 32768, 32959 },
		{ 76, 32704, 32831, 32768, 32895 },
		{ 77, 32704, 32831, 32768, 32895 },
		{ 78, 32832, 32959, 32704, 32831 },
		{ 79, 32704, 32831, 32768, 32895 },
		{ 80, 32704, 32831, 32768, 32895 },
		{ 81, 32704, 32831, 32768, 32895 },
		{ 82, 32640, 32767, 32768, 32895 },
		{ 83, 32704, 32831, 32768, 32831 },
		{ 84, 32704, 32767, 32768, 32831 },
		{ 85, 32512, 32831, 32640, 32959 },
		{ 86, 32768, 33023, 32704, 32895 },
		{ 87, 32704, 32767, 32768, 32831 },
		{ 88, 33472, 33599, 32640, 32767 },
		{ 89, 32640, 32767, 32832, 32959 },
		{ 90, 32640, 32767, 32832, 32959 },
		{ 91, 32640, 32767, 32832, 32959 },
		{ 92, 32640, 32767, 32832, 32959 },
		{ 93, 32640, 32767, 32832, 32959 },
		{ 94, 32640, 32767, 32832, 32959 },
		{ 95, 32640, 32767, 32832, 32959 },
		{ 96, 32640, 32767, 32832, 32959 },
		{ 97, 32640, 32767, 32832, 32959 },
		{ 98, 32640, 32767, 32832, 32959 },
		{ 99, 32704, 32767, 32768, 32831 },
		{ 100, 32704, 32831, 32768, 32895 },
		{ 101, 32704, 32895, 32704, 32895 },
		{ 102, 32704, 32895, 32704, 32895 },
		{ 103, 32704, 32895, 32704, 32895 },
		{ 104, 32576, 32767, 32768, 32959 },
		{ 105, 32576, 32767, 32768, 32959 },
		{ 106, 32576, 33855, 32768, 32959 },
		{ 107, 32576, 32767, 32768, 32959 },
		{ 108, 32576, 32767, 32768, 32959 },
		{ 109, 32576, 32767, 32768, 32959 },
		{ 110, 32704, 32895, 32704, 32895 },
		{ 111, 32576, 32767, 32768, 32959 },
		{ 112, 32704, 32895, 32704, 32895 },
		{ 113, 32704, 32895, 32704, 32895 },
		{ 114, 32576, 32767, 32768, 32959 },
		{ 115, 32576, 32767, 32768, 32959 },
		{ 116, 32704, 32831, 32832, 32895 },
		{ 117, 32576, 32767, 32768, 32959 },
		{ 118, 32576, 32767, 32768, 32959 },
		{ 119, 32576, 32767, 32768, 32959 },
		{ 120, 32704, 32895, 32704, 32895 },
		{ 121, 32576, 32767, 32768, 32959 },
		{ 122, 32704, 32895, 32704, 32895 },
		{ 123, 32704, 32895, 32704, 32895 },
		{ 124, 32576, 32767, 32768, 32959 },
		{ 125, 32576, 32767, 32768, 32959 },
		{ 126, 32704, 32831, 32832, 32895 },
		{ 127, 32576, 32767, 32768, 32959 },
		{ 128, 32576, 32767, 32768, 32959 },
		{ 129, 32576, 32767, 32768, 32959 },
		{ 130, 32704, 32895, 32704, 32895 },
		{ 131, 32576, 32767, 32768, 32959 },
		{ 132, 32704, 32895, 32704, 32895 },
		{ 133, 32704, 32895, 32704, 32895 },
		{ 134, 32576, 32767, 32768, 32959 },
		{ 135, 32576, 32767, 32768, 32959 },
		{ 136, 32704, 32831, 32832, 32895 },
		{ 137, 32576, 32767, 32768, 32959 },
		{ 138, 32576, 32767, 32768, 32959 },
		{ 139, 32576, 32767, 32768, 32959 },
		{ 140, 32704, 32895, 32704, 32895 },
		{ 141, 32576, 32767, 32768, 32959 },
		{ 142, 32704, 32895, 32704, 32895 },
		{ 143, 32704, 32895, 32704, 32895 },
		{ 144, 32576, 32767, 32768, 32959 },
		{ 145, 32576, 32767, 32768, 32959 },
		{ 146, 32576, 32831, 32768, 32959 },
		{ 147, 32576, 32767, 32768, 32959 },
		{ 148, 32576, 32767, 32768, 32959 },
		{ 149, 32576, 32767, 32768, 32959 },
		{ 150, 32704, 32895, 32704, 32895 },
		{ 151, 32576, 32767, 32768, 32959 },
		{ 152, 32576, 32767, 32768, 32959 },
		{ 153, 32576, 32767, 32768, 32959 },
		{ 154, 32704, 32895, 32704, 32895 },
		{ 155, 32704, 32895, 32704, 32895 },
		{ 156, 32704, 32831, 32768, 32831 },
		{ 157, 32576, 32767, 32768, 32959 },
		{ 158, 32576, 32767, 32768, 32959 },
		{ 159, 32576, 32767, 32768, 32959 },
		{ 160, 32576, 32767, 32768, 32959 },
		{ 161, 32576, 32767, 32768, 32959 },
		{ 162, 32576, 32767, 32768, 32959 },
		{ 163, 32576, 32767, 32768, 32959 },
		{ 164, 32704, 32895, 32704, 32895 },
		{ 165, 32704, 32895, 32704, 32895 },
		{ 166, 32704, 32831, 32768, 32831 },
		{ 167, 32576, 32767, 32768, 32959 },
		{ 168, 32576, 32767, 32768, 32959 },
		{ 169, 32576, 32767, 32768, 32959 },
		{ 170, 32576, 32767, 32768, 32959 },
		{ 171, 32576, 32767, 32768, 32959 },
		{ 172, 32576, 32767, 32768, 32959 },
		{ 173, 32576, 32767, 32768, 32959 },
		{ 174, 32704, 32895, 32704, 32895 },
		{ 175, 32704, 32895, 32704, 32895 },
		{ 176, 32704, 32831, 32768, 32831 },
		{ 177, 32576, 32767, 32768, 32959 },
		{ 178, 32576, 32767, 32768, 32959 },
		{ 179, 32576, 32767, 32768, 32959 },
		{ 180, 32576, 32767, 32768, 32959 },
		{ 181, 32576, 32767, 32768, 32959 },
		{ 182, 32576, 32767, 32768, 32959 },
		{ 183, 32576, 32767, 32768, 32959 },
		{ 184, 32704, 32895, 32704, 32895 },
		{ 185, 32704, 32895, 32704, 32895 },
		{ 186, 32704, 32831, 32768, 32831 },
		{ 187, 32576, 32767, 32768, 32959 },
		{ 188, 32576, 32767, 32768, 32959 },
		{ 189, 32576, 32767, 32768, 32959 },
		{ 190, 32576, 32767, 32768, 32959 },
		{ 191, 32576, 32767, 32768, 32959 },
		{ 192, 32576, 32767, 32768, 32959 },
		{ 193, 32576, 32767, 32768, 32959 },
		{ 194, 32704, 32895, 32704, 32895 },
		{ 195, 32704, 32895, 32704, 32895 },
		{ 196, 32704, 32831, 32768, 32831 },
		{ 197, 32576, 32767, 32768, 32959 },
		{ 198, 32576, 32767, 32768, 32959 },
		{ 199, 32576, 32767, 32768, 32959 },
		{ 200, 32576, 32831, 32768, 33023 },
		{ 201, 32640, 33535, 32704, 32959 },
		{ 202, 32640, 33535, 32704, 32895 },
		{ 203, 32640, 33535, 32704, 32895 },
		{ 204, 32640, 33535, 32704, 32895 },
		{ 205, 32640, 33535, 32704, 32895 },
		{ 206, 32640, 33535, 32704, 32895 },
		{ 207, 32640, 33535, 32704, 32895 },
		{ 208, 32640, 33535, 32704, 32895 },
		{ 209, 32704, 32767, 32768, 32831 },
		{ 210, 32640, 33535, 32704, 32895 },
		{ 211, 32640, 33535, 32704, 32895 },
		{ 213, 32704, 32767, 32768, 32831 },
		{ 217, 32640, 32767, 32768, 32831 },
		{ 221, 32640, 32895, 32640, 32895 },
		{ 237, 32704, 32767, 32768, 32831 },
		{ 240, 32640, 32767, 33024, 33151 },
		{ 241, 32704, 32831, 32832, 32959 },
		{ 242, 32704, 32831, 32896, 33087 },
		{ 243, 32640, 32767, 32832, 33023 },
		{ 244, 32704, 33023, 32768, 33087 },
		{ 248, 32704, 32831, 32768, 32895 },
		{ 249, 32704, 32831, 32832, 32895 },
		{ 250, 32704, 32831, 32768, 32895 },
		{ 251, 32768, 32831, 32768, 32831 },
		{ 252, 32576, 32767, 32832, 32895 },
		{ 253, 32704, 32831, 32832, 33023 },
		{ 254, 32576, 32767, 32768, 32959 },
		{ 255, 32640, 32831, 32768, 32895 },
		{ 256, 32640, 32895, 32768, 33023 },
		{ 257, 32640, 32831, 32768, 32895 },
		{ 258, 32576, 32831, 32768, 33151 },
		{ 259, 32704, 32767, 32768, 33023 },
		{ 261, 32704, 32895, 32768, 32959 },
		{ 262, 32704, 32895, 32768, 32959 },
		{ 263, 32704, 32895, 32768, 32959 },
		{ 264, 32704, 32895, 32768, 32959 },
		{ 265, 32704, 32895, 32768, 32959 },
		{ 266, 32704, 32895, 32768, 32959 },
		{ 267, 32704, 32895, 32768, 32959 },
		{ 268, 32704, 32895, 32768, 32959 },
		{ 269, 32704, 32895, 32768, 32959 },
		{ 270, 32704, 32767, 32768, 32831 },
		{ 271, 32704, 32831, 32768, 32895 },
		{ 272, 32704, 32831, 32768, 32895 },
		{ 273, 32704, 32831, 32768, 32895 },
		{ 274, 32704, 32831, 32768, 32895 },
		{ 275, 32704, 32831, 32768, 32895 },
		{ 276, 32704, 32831, 32768, 32895 },
		{ 277, 32704, 32831, 32768, 32895 },
		{ 278, 32704, 32895, 32768, 32895 },
		{ 279, 32704, 32895, 32768, 32959 },
		{ 280, 32832, 32959, 32704, 32831 },
		{ 281, 32704, 32831, 32768, 32895 },
		{ 282, 32704, 32831, 32768, 32895 },
		{ 283, 32704, 32831, 32768, 32895 },
		{ 284, 32640, 32767, 32768, 32895 },
		{ 285, 32832, 32959, 32704, 32831 },
		{ 286, 32704, 32831, 32768, 32895 },
		{ 287, 32704, 32831, 32768, 32895 },
		{ 288, 32704, 32831, 32768, 32895 },
		{ 289, 32640, 32767, 32768, 32895 },
		{ 290, 32704, 32895, 32768, 32959 },
		{ 291, 32704, 32895, 32768, 32959 },
		{ 300, 32832, 32959, 32448, 32639 },
		{ 301, 32576, 32767, 32768, 33023 },
		{ 302, 32704, 32767, 32832, 32895 },
		{ 303, 32576, 32959, 32512, 32895 },
		{ 304, 32576, 32959, 32768, 33023 },
		{ 305, 32704, 32767, 32768, 32831 },
		{ 306, 32512, 32767, 32768, 32959 },
		{ 307, 32704, 32959, 32768, 32959 },
		{ 308, 32704, 33023, 32768, 32959 },
		{ 309, 32704, 33087, 32768, 32959 },
		{ 310, 32640, 32895, 32768, 33023 },
		{ 320, 32640, 33087, 32768, 33087 },
		{ 330, 32640, 32831, 32768, 33023 },
		{ 340, 32640, 32895, 32704, 32959 },
		{ 350, 32640, 32767, 32768, 32895 },
		{ 360, 32640, 32831, 32704, 32895 },
		{ 370, 32640, 32831, 32704, 32895 },
		{ 400, 32448, 33087, 32576, 33087 },
		{ 401, 32704, 32895, 32768, 32895 },
		{ 410, 32640, 32959, 32704, 33087 },
		{ 420, 32640, 32831, 32832, 33151 },
		{ 430, 32640, 33023, 32704, 33087 },
		{ 440, 32256, 32767, 32768, 33279 },
		{ 441, 32640, 32895, 32704, 32959 },
		{ 442, 32704, 32895, 32704, 32895 },
		{ 443, 32640, 32895, 32640, 32959 },
		{ 444, 32704, 32767, 32768, 32831 },
		{ 445, 32704, 32831, 32832, 32895 },
		{ 446, 32704, 32831, 32768, 32831 },
		{ 447, 32704, 32767, 32768, 32831 },
		{ 450, 32576, 32831, 32768, 32959 },
		{ 451, 32704, 32895, 32704, 32895 },
		{ 452, 32704, 32831, 32768, 32895 },
		{ 453, 32704, 32895, 32704, 32895 },
		{ 454, 32704, 32895, 32704, 32895 },
		{ 455, 32704, 32895, 32768, 32895 },
		{ 456, 32704, 32831, 32768, 32895 },
		{ 457, 32640, 32767, 32768, 32895 },
		{ 460, 32704, 32895, 32768, 32895 },
		{ 461, 32640, 32895, 32768, 32895 },
		{ 462, 32640, 32895, 32768, 32895 },
		{ 463, 32704, 32831, 32768, 32895 },
		{ 464, 32704, 32831, 32768, 32895 },
		{ 465, 32704, 32831, 32768, 32895 },
		{ 466, 32704, 32831, 32768, 32895 },
		{ 467, 32640, 32767, 32768, 32895 },
		{ 468, 32576, 32703, 32832, 32959 },
		{ 470, 32640, 32895, 32768, 32895 },
		{ 471, 32704, 32831, 32768, 32895 },
		{ 472, 32640, 32831, 32704, 32895 },
		{ 473, 32704, 32959, 32768, 32895 },
		{ 474, 32704, 32831, 32768, 32895 },
		{ 475, 32640, 32831, 32768, 32895 },
		{ 476, 32704, 32831, 32704, 32895 },
		{ 477, 32704, 32831, 32768, 32959 },
		{ 478, 32640, 32831, 32704, 32895 },
		{ 480, 32576, 32895, 32768, 33023 },
		{ 481, 32640, 32831, 32768, 32895 },
		{ 482, 32576, 32831, 32704, 32895 },
		{ 483, 32640, 32895, 32704, 32959 },
		{ 484, 32704, 32895, 32704, 32895 },
		{ 490, 32640, 32767, 32768, 32895 },
		{ 491, 32640, 32767, 32704, 32895 },
		{ 492, 32704, 32895, 32768, 32895 },
		{ 493, 32704, 32831, 32704, 32831 },
		{ 494, 32768, 32895, 32704, 32831 },
		{ 495, 32704, 32895, 32704, 32895 },
		{ 496, 32768, 32895, 32768, 32895 },
		{ 500, 32640, 32831, 32704, 32895 },
		{ 501, 32576, 33215, 32512, 32959 },
		{ 502, 32576, 33215, 32576, 32959 },
		{ 503, 32576, 33215, 32576, 32959 },
		{ 504, 32576, 33215, 32576, 32959 },
		{ 505, 32576, 33215, 32576, 32959 },
		{ 506, 32512, 33215, 32576, 32959 },
		{ 507, 32704, 33023, 32768, 32959 },
		{ 508, 32768, 32895, 33088, 33215 },
		{ 509, 32704, 32767, 32768, 32831 },
		{ 511, 32576, 33215, 32576, 32895 },
		{ 512, 32576, 33215, 32576, 32895 },
		{ 513, 32576, 33215, 32576, 32895 },
		{ 514, 32576, 33215, 32576, 32895 },
		{ 515, 32576, 33215, 32576, 32895 },
		{ 516, 32576, 33215, 32576, 32895 },
		{ 518, 32640, 32831, 32704, 32895 },
		{ 521, 32576, 32767, 32832, 33023 },
		{ 522, 32576, 32831, 32832, 32959 },
		{ 523, 32576, 32831, 32832, 32959 },
		{ 524, 32576, 32831, 32832, 32959 },
		{ 530, 32704, 32959, 32768, 32959 },
		{ 531, 32704, 32895, 32704, 32959 },
		{ 532, 32704, 32895, 32768, 32895 },
		{ 533, 32704, 32895, 32768, 33023 },
		{ 534, 32704, 32959, 32768, 32895 },
		{ 535, 32576, 32895, 32704, 33023 },
		{ 536, 32640, 32831, 32704, 32895 },
		{ 541, 32640, 32831, 32768, 32895 },
		{ 542, 32640, 32831, 32768, 32895 },
		{ 543, 32704, 32895, 32704, 32895 },
		{ 550, 32384, 32895, 32640, 33151 },
		{ 551, 32640, 32767, 32768, 32895 },
		{ 552, 32704, 32767, 32768, 32895 },
		{ 553, 32704, 32831, 32768, 32895 },
		{ 554, 32704, 32831, 32768, 32895 },
		{ 555, 32704, 32767, 32768, 32895 },
		{ 556, 32704, 32831, 32768, 32895 },
		{ 557, 32704, 32831, 32768, 32895 },
		{ 558, 32704, 33215, 32768, 33279 },
		{ 600, 32640, 32895, 32704, 32895 },
		{ 601, 32640, 32959, 32704, 32959 },
		{ 602, 32640, 32767, 32768, 32895 },
		{ 603, 32640, 32767, 32768, 32895 },
		{ 604, 32768, 32895, 32768, 32895 },
		{ 605, 32704, 32959, 32704, 32959 },
		{ 606, 32640, 32895, 32704, 32895 },
		{ 607, 32640, 32895, 32768, 32959 },
		{ 608, 32640, 32767, 32832, 32959 },
		{ 610, 32640, 32895, 32704, 32959 },
		{ 611, 32640, 32895, 32640, 32831 },
		{ 612, 32640, 32895, 32704, 32959 },
		{ 613, 32640, 32895, 32768, 33023 },
		{ 620, 32768, 32831, 32768, 32831 },
		{ 621, 32640, 32895, 32768, 32959 },
		{ 630, 32704, 33023, 32768, 33087 },
		{ 631, 32704, 33087, 32640, 33023 },
		{ 632, 32640, 32959, 32576, 32895 },
		{ 666, 32640, 32831, 32704, 32895 },
		{ 701, 32576, 33023, 32512, 32959 },
		{ 725, 33472, 33663, 32576, 32767 },
		{ 726, 32640, 32767, 32832, 32959 },
		{ 777, 32448, 32831, 32832, 33151 },
		{ 778, 32640, 32959, 32576, 33023 },
		{ 779, 32768, 33023, 32704, 32895 },
		{ 780, 32576, 32831, 32704, 33087 },
		{ 781, 32704, 33023, 32704, 32895 },
		{ 782, 32704, 32831, 32768, 32895 },
		{ 783, 32704, 33343, 32576, 32959 },
		{ 784, 32704, 32831, 32832, 32895 },
		{ 785, 32640, 32959, 32576, 32895 },
		{ 786, 32640, 32959, 32576, 32895 },
		{ 787, 32640, 32959, 32576, 32895 },
		{ 997, 32704, 32767, 32768, 32831 },
		{ 998, 32704, 32767, 32768, 32831 },
		{ 1000, 32704, 32895, 32768, 32959 },
		{ 1001, 32704, 32895, 32768, 32959 },
		{ 1002, 32576, 33087, 32512, 33023 },
		{ 1003, 32640, 32895, 32832, 32959 },
		{ 1004, 32512, 32959, 32512, 32895 },
		{ 1005, 32512, 33023, 32576, 33023 },
		{ 1006, 32512, 33023, 32576, 33023 },
		{ 1007, 32512, 33023, 32576, 33023 },
		{ 1008, 32512, 33023, 32576, 33023 },
		{ 1009, 32512, 33023, 32576, 33023 },
		{ 1010, 32512, 33023, 32576, 33023 },
		{ 1011, 32640, 33151, 32448, 32959 },
		{ 1012, 32640, 33151, 32448, 32959 },
		{ 1013, 32640, 33151, 32448, 32959 },
		{ 1014, 32640, 33151, 32448, 32959 },
		{ 1015, 32640, 33151, 32448, 32959 },
		{ 1016, 32640, 33151, 32448, 32959 },
		{ 1017, 32512, 33023, 32768, 33279 },
		{ 1018, 32512, 33023, 32768, 33279 },
		{ 1019, 32512, 33023, 32768, 33279 },
		{ 1020, 32512, 33023, 32768, 33279 },
		{ 1021, 32512, 33023, 32768, 33279 },
		{ 1022, 32512, 33023, 32768, 33279 },
		{ 1023, 32512, 32959, 32704, 33087 }, 
		{ 2000, 32640, 32959, 32704, 33023 },
		{ 2001, 32640, 32959, 32704, 33023 },
		{ 2002, 32640, 32959, 32704, 33023 },
		{ 2003, 32640, 32959, 32704, 33023 },
		{ 2004, 32640, 32895, 32768, 32959 },
		{ 2005, 32512, 32895, 32704, 33087 },
		{ 2006, 32704, 32895, 32768, 32959 },
		{ 2007, 32512, 32703, 32640, 32895 },
		{ 2010, 32576, 33087, 32512, 32831 },
		{ 2011, 32576, 33087, 32512, 32831 },
		{ 2100, 32768, 32831, 32768, 32831 },
		{ 2101, 32704, 32895, 32768, 32959 },
		{ 2151, 32704, 32895, 32768, 32959 },
		{ 2201, 32704, 32895, 32768, 32959 },
		{ 2202, 32704, 32895, 32768, 32959 },
		{ 2203, 32704, 32895, 32768, 32959 },
		{ 4301, 32704, 32959, 32768, 33023 },
		{ 4831, 32704, 32831, 32768, 32959 },
		{ 4941, 32768, 32895, 32704, 32831 },
		{ 5001, 32704, 32831, 32704, 32831 },
		{ 5002, 32704, 32831, 32704, 32831 },
		{ 5003, 32704, 32831, 32704, 32831 },
		{ 5004, 32704, 32831, 32704, 32831 },
		{ 5005, 32704, 32831, 32704, 32831 },
		{ 5006, 32704, 32831, 32704, 32831 },
		{ 5007, 32704, 32831, 32704, 32831 },
		{ 5008, 32704, 32831, 32704, 32831 },
		{ 5009, 32704, 32831, 32704, 32831 },
		{ 5010, 32704, 32831, 32704, 32831 },
		{ 5011, 32704, 32831, 32704, 32831 },
		{ 5012, 32704, 32831, 32704, 32831 },
		{ 5013, 32704, 32831, 32704, 32831 },
		{ 5014, 32704, 32831, 32704, 32831 },
		{ 5015, 32704, 32831, 32704, 32831 },
		{ 5016, 32704, 32831, 32704, 32831 },
		{ 5017, 32704, 32831, 32704, 32831 },
		{ 5018, 32704, 32831, 32704, 32831 },
		{ 5019, 32704, 32831, 32704, 32831 },
		{ 5020, 32704, 32831, 32704, 32831 },
		{ 5021, 32704, 32831, 32704, 32831 },
		{ 5022, 32704, 32831, 32704, 32831 },
		{ 5023, 32704, 32831, 32704, 32831 },
		{ 5024, 32704, 32831, 32704, 32831 },
		{ 5025, 32704, 32831, 32704, 32831 },
		{ 5026, 32704, 32831, 32704, 32831 },
		{ 5027, 32704, 32831, 32704, 32831 },
		{ 5028, 32704, 32831, 32704, 32831 },
		{ 5029, 32704, 32831, 32704, 32831 },
		{ 5030, 32704, 32831, 32704, 32831 },
		{ 5031, 32704, 32831, 32704, 32831 },
		{ 5032, 32704, 32831, 32704, 32831 },
		{ 5033, 32704, 32831, 32704, 32831 },
		{ 5034, 32704, 32831, 32704, 32831 },
		{ 5035, 32704, 32831, 32704, 32831 },
		{ 5036, 32704, 32831, 32704, 32831 },
		{ 5037, 32704, 32831, 32704, 32831 },
		{ 5038, 32704, 32831, 32704, 32831 },
		{ 5039, 32704, 32831, 32704, 32831 },
		{ 5040, 32704, 32831, 32704, 32831 },
		{ 5041, 32704, 32831, 32704, 32831 },
		{ 5042, 32704, 32831, 32704, 32831 },
		{ 5043, 32704, 32831, 32704, 32831 },
		{ 5044, 32704, 32831, 32704, 32831 },
		{ 5045, 32704, 32831, 32704, 32831 },
		{ 5046, 32704, 32831, 32704, 32831 },
		{ 5047, 32704, 32831, 32704, 32831 },
		{ 5048, 32704, 32831, 32704, 32831 },
		{ 5049, 32704, 32831, 32704, 32831 },
		{ 5050, 32704, 32831, 32704, 32831 },
		{ 5051, 32704, 32831, 32704, 32831 },
		{ 5052, 32704, 32831, 32704, 32831 },
		{ 5053, 32704, 32831, 32704, 32831 },
		{ 5054, 32704, 32831, 32704, 32831 },
		{ 5055, 32704, 32831, 32704, 32831 },
		{ 5056, 32704, 32831, 32704, 32831 },
		{ 5057, 32704, 32831, 32704, 32831 },
		{ 5058, 32704, 32831, 32704, 32831 },
		{ 5059, 32704, 32831, 32704, 32831 },
		{ 5060, 32704, 32831, 32704, 32831 },
		{ 5061, 32704, 32831, 32704, 32831 },
		{ 5062, 32704, 32831, 32704, 32831 },
		{ 5063, 32704, 32831, 32704, 32831 },
		{ 5064, 32704, 32831, 32704, 32831 },
		{ 5065, 32704, 32831, 32704, 32831 },
		{ 5066, 32704, 32831, 32704, 32831 },
		{ 5067, 32704, 32831, 32704, 32831 },
		{ 5068, 32704, 32831, 32768, 32895 },
		{ 5069, 32704, 32831, 32768, 32895 },
		{ 5070, 32704, 32831, 32768, 32895 },
		{ 5071, 32704, 32831, 32768, 32895 },
		{ 5072, 32704, 32831, 32768, 32895 },
		{ 5073, 32704, 32831, 32768, 32895 },
		{ 5074, 32704, 32831, 32768, 32895 },
		{ 5075, 32704, 32831, 32768, 32895 },
		{ 5076, 32704, 32831, 32768, 32895 },
		{ 5077, 32704, 32831, 32768, 32895 },
		{ 5078, 32704, 32831, 32768, 32895 },
		{ 5079, 32704, 32831, 32768, 32895 },
		{ 5080, 32704, 32831, 32768, 32895 },
		{ 5081, 32704, 32831, 32768, 32895 },
		{ 5082, 32704, 32831, 32768, 32895 },
		{ 5083, 32704, 32831, 32768, 32895 },
		{ 5084, 32704, 32831, 32768, 32895 },
		{ 5085, 32704, 32831, 32768, 32895 },
		{ 5086, 32704, 32831, 32768, 32895 },
		{ 5087, 32704, 32831, 32768, 32895 },
		{ 5088, 32704, 32831, 32768, 32895 },
		{ 5089, 32704, 32831, 32768, 32895 },
		{ 5090, 32704, 32831, 32768, 32895 },
		{ 5091, 32704, 32831, 32768, 32895 },
		{ 5092, 32704, 32831, 32768, 32895 },
		{ 5093, 32704, 32831, 32768, 32895 },
		{ 5094, 32704, 32831, 32768, 32895 },
		{ 5095, 32704, 32831, 32768, 32895 },
		{ 5096, 32704, 32831, 32768, 32895 },
		{ 5097, 32704, 32831, 32768, 32895 },
		{ 5098, 32704, 32831, 32768, 32895 },
		{ 5099, 32704, 32831, 32768, 32895 },
		{ 5100, 32704, 32831, 32768, 32895 },
		{ 5101, 32704, 32831, 32768, 32895 },
		{ 5102, 32704, 32831, 32768, 32895 },
		{ 5103, 32704, 32831, 32768, 32895 },
		{ 5104, 32704, 32831, 32768, 32895 },
		{ 5105, 32704, 32831, 32768, 32895 },
		{ 5106, 32704, 32831, 32768, 32895 },
		{ 5107, 32704, 32831, 32768, 32895 },
		{ 5108, 32704, 32831, 32768, 32895 },
		{ 5109, 32704, 32831, 32768, 32895 },
		{ 5110, 32704, 32831, 32768, 32895 },
		{ 5111, 32704, 32831, 32768, 32895 },
		{ 5112, 32704, 32831, 32768, 32895 },
		{ 5113, 32704, 32831, 32768, 32895 },
		{ 5114, 32704, 32831, 32768, 32895 },
		{ 5115, 32704, 32831, 32768, 32895 },
		{ 5116, 32704, 32831, 32768, 32895 },
		{ 5117, 32704, 32831, 32768, 32895 },
		{ 5118, 32704, 32831, 32768, 32895 },
		{ 5119, 32704, 32831, 32768, 32895 },
		{ 5120, 32704, 32831, 32768, 32895 },
		{ 5121, 32704, 32831, 32768, 32895 },
		{ 5122, 32704, 32831, 32768, 32895 },
		{ 5123, 32704, 32831, 32768, 32895 },
		{ 5124, 32704, 32895, 32704, 32895 },
		{ 5125, 32768, 32895, 32768, 32895 },
		{ 5131, 32768, 32831, 32832, 32895 },
		{ 5132, 32768, 32831, 32832, 32895 },
		{ 5133, 32768, 32831, 32832, 32895 },
		{ 5134, 32768, 32831, 32832, 32895 },
		{ 5140, 32640, 32959, 32704, 32959 },
		{ 5141, 32640, 32959, 32704, 32959 },
		{ 5142, 32640, 32959, 32704, 32959 },
		{ 5143, 32704, 32831, 32768, 32895 },
		{ 5144, 32704, 32831, 32768, 32895 },
		{ 5145, 32704, 32831, 32768, 32895 },
		{ 5153, 32576, 32703, 32832, 32959 },
		{ 5166, 32704, 32831, 32768, 32895 },
		{ 5167, 32512, 32767, 32704, 32959 },
		{ 5168, 32512, 32767, 32704, 32959 },
		{ 5169, 32512, 32767, 32704, 32959 },
		{ 5170, 32512, 32767, 32704, 32959 },
		{ 5171, 32512, 32767, 32704, 32959 },
		{ 5172, 32512, 32767, 32704, 32959 },
		{ 5173, 32512, 32767, 32704, 32959 },
		{ 5174, 32512, 32767, 32704, 32959 },
		{ 5175, 32512, 32767, 32704, 32959 },
		{ 5176, 32512, 32767, 32704, 32959 },
		{ 5177, 32512, 32767, 32704, 32959 },
		{ 5178, 32512, 32767, 32704, 32959 },
		{ 5179, 32512, 32767, 32704, 32959 },
		{ 5180, 32512, 32767, 32704, 32959 },
		{ 5181, 32512, 32767, 32704, 32959 },
		{ 5182, 32512, 32767, 32704, 32959 },
		{ 5183, 32512, 32767, 32704, 32959 },
		{ 5184, 32512, 32767, 32704, 32959 },
		{ 5185, 32512, 32767, 32704, 32959 },
		{ 5186, 32512, 32767, 32704, 32959 },
		{ 5187, 32512, 32767, 32704, 32959 },
		{ 5188, 32512, 32767, 32704, 32959 },
		{ 5189, 32512, 32767, 32704, 32959 },
		{ 5190, 32512, 32767, 32704, 32959 },
		{ 5191, 32704, 32895, 32768, 32959 },
		{ 5192, 32704, 32895, 32768, 32959 },
		{ 5193, 32704, 32895, 32768, 32959 },
		{ 5194, 32704, 32895, 32768, 32959 },
		{ 5195, 32704, 32895, 32768, 32959 },
		{ 5196, 32704, 32895, 32768, 32959 },
		{ 5197, 32704, 32895, 32768, 32959 },
		{ 5198, 32704, 32895, 32768, 32959 },
		{ 5199, 32704, 32895, 32768, 32959 },
		{ 5200, 32704, 32895, 32768, 32959 },
		{ 5300, 32640, 32895, 32704, 32959 },
		{ 5301, 32640, 32895, 32704, 32959 },
		{ 5302, 32640, 32895, 32704, 32959 },
		{ 5303, 32704, 32895, 32768, 32959 },
		{ 5384, 32704, 32895, 32640, 32831 },
		{ 5435, 32704, 32959, 32704, 32959 },
		{ 5501, 32704, 32959, 32832, 33087 },
		{ 5551, 32704, 32959, 32832, 33087 },
		{ 5554, 32640, 32895, 32704, 32959 },
		{ 5701, 32576, 33023, 32512, 32959 },
		{ 5801, 32512, 32831, 32704, 33023 },
		{ 5802, 32512, 32831, 32704, 33023 },
		{ 5803, 32512, 32831, 32704, 33023 },
		{ 5804, 32512, 32831, 32704, 33023 },
		{ 5805, 32512, 32831, 32704, 33023 },
		{ 6041, 32768, 32895, 32768, 32895 },
		{ 6051, 32768, 32895, 32768, 32895 },
		{ 6201, 32768, 33151, 32768, 33151 },
		{ 6202, 32768, 33151, 32768, 33151 },
		{ 6203, 32768, 33151, 32768, 33151 },
		{ 6301, 32704, 33023, 32768, 33087 },
		{ 6302, 32640, 33023, 32576, 33087 },
		{ 6303, 32640, 33023, 32576, 33087 },
		{ 6311, 32704, 33023, 32640, 32959 },
		{ 6312, 32640, 33023, 32576, 32959 },
		{ 6313, 32640, 33023, 32576, 32959 },
		{ 6321, 32640, 32959, 32576, 32895 },
		{ 6322, 32640, 32959, 32576, 32895 },
		{ 6323, 32640, 32959, 32576, 32895 },
		{ 6801, 32640, 33023, 32576, 32895 },
		{ 6901, 32512, 32831, 32704, 33023 },
		{ 7001, 32576, 33023, 32640, 33087 },
		{ 7100, 32704, 32959, 32832, 33087 },
		{ 9000, 32576, 32895, 32640, 33023 },
		{ 9100, 32704, 32767, 32832, 32895 },
		{ 9101, 32640, 32831, 32768, 32895 },
		{ 9102, 32704, 32831, 32768, 32895 },
		{ 9202, 32704, 32767, 32832, 32895 },
		{ 9990, 32640, 32959, 32576, 32895 },
		{ 9991, 32640, 32959, 32576, 32895 },
		{ 9992, 32640, 32959, 32576, 32895 },
		{ 9993, 32640, 32959, 32576, 32895 },
		{ 9994, 32640, 32959, 32576, 32895 },
		{ 9995, 32640, 32959, 32576, 32895 },
		{ 9996, 32640, 32959, 32576, 32895 },
		{ 9997, 32640, 32959, 32576, 32895 },
		{ 9998, 32640, 32959, 32576, 32895 },
		{ 9999, 32640, 32959, 32576, 32895 },
		{ 16384, 32704, 32767, 32768, 32831 },
		{ 16896, 32704, 32767, 32768, 32831 },
		{ 17408, 32704, 32767, 32768, 32831 },
		{ 17920, 32704, 32767, 32768, 32831 },
		{ 18432, 32704, 32767, 32768, 32831 },
		{ 18944, 32704, 32767, 32768, 32831 },
		{ 19456, 32704, 32767, 32768, 32831 },
		{ 19968, 32704, 32767, 32768, 32831 },
		{ 20480, 32704, 32767, 32768, 32831 },
		{ 20992, 32704, 32767, 32768, 32831 },
		{ 21504, 32704, 32767, 32768, 32831 },
		{ 22016, 32704, 32767, 32768, 32831 },
		{ 22528, 32704, 32767, 32768, 32831 },
		{ 23040, 32704, 32767, 32768, 32831 },
		{ 23552, 32704, 32767, 32768, 32831 },
		{ 24064, 32704, 32767, 32768, 32831 },
		{ 24576, 32704, 32767, 32768, 32831 },
		{ 25088, 32704, 32767, 32768, 32831 } };
	
}
