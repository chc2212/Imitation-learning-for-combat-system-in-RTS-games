package jnibwapi;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jnibwapi.model.Map;
import jnibwapi.model.Player;
import jnibwapi.model.Region;
import jnibwapi.model.Unit;
import jnibwapi.types.*;

/**
 * JNI interface for the Brood War API.<br>
 * 
 * This focus of this interface is to provide the callback and game state query functionality in
 * BWAPI.<br>
 * 
 * Note: for thread safety and game state sanity, all native calls should be invoked from the
 * callback methods.<br>
 * 
 * For BWAPI documentation see: {@link http://code.google.com/p/bwapi/}<br>
 * 
 * API Pages<br>
 * Game: {@link http://code.google.com/p/bwapi/wiki/Game}<br>
 * Unit: {@link http://code.google.com/p/bwapi/wiki/Unit}<br>
 */
public class JNIBWAPI {
	
	//////////////////
	int count = 0;
	//////////////////
	
	// load the BWAPI client library
	static {
		try {
			System.loadLibrary("client-bridge-" + System.getProperty("os.arch"));
			System.out.println("Loaded client bridge library.");
		} catch (UnsatisfiedLinkError e) {
			// Help beginners put the DLL in the correct place (although anywhere on the path will
			// work)
			File dll = new File("client-bridge-" + System.getProperty("os.arch") + ".dll");
			if (!dll.exists()) {
				System.err.println("Native code library not found: " + dll.getAbsolutePath());
			}
			System.err.println("Native code library failed to load: " + e.toString());
		}
	}
	
	/** callback listener for BWAPI events */
	private BWAPIEventListener listener;
	/** whether to use BWTA for map analysis */
	private boolean enableBWTA;
	private Charset charset;
	
	/**
	 * Instantiates a BWAPI instance, but does not connect to the bridge. To connect, the start
	 * method must be invoked.
	 * 
	 * @param listener - listener for BWAPI callback events.
	 * @param enableBWTA - whether to use BWTA for map analysis
	 */
	public JNIBWAPI(BWAPIEventListener listener, boolean enableBWTA) {
		this.listener = listener;
		this.enableBWTA = enableBWTA;
		try {
			// Using the Korean character set for decoding byte[]s into Strings will allow Korean
			// characters to be parsed correctly.
			charset = Charset.forName("Cp949");
		} catch (UnsupportedCharsetException e) {
			System.out.println(
					"Korean character set not available. Some characters may not be read properly");
			charset = StandardCharsets.ISO_8859_1;
		}
	}
	
	/**
	 * Invokes the native library which will connect to the bridge and then invoke callback
	 * functions.
	 * 
	 * Note: this method never returns, it should be invoked from a separate thread if concurrent
	 * java processing is needed.
	 */
	public void start() {
		startClient(this);
	}
	
	// game state
	private int gameFrame = 0;
	private Map map;
	private HashMap<Integer, Unit> units = new HashMap<Integer, Unit>();
	private ArrayList<Unit> playerUnits = new ArrayList<Unit>();
	private ArrayList<Unit> alliedUnits = new ArrayList<Unit>();
	private ArrayList<Unit> enemyUnits = new ArrayList<Unit>();
	private ArrayList<Unit> neutralUnits = new ArrayList<Unit>();
	
	// player lists
	private Player self;
	private Player neutralPlayer;
	private HashSet<Integer> allyIDs = new HashSet<Integer>();
	private HashSet<Integer> enemyIDs = new HashSet<Integer>();
	private HashMap<Integer, Player> players = new HashMap<Integer, Player>();
	private ArrayList<Player> allies = new ArrayList<Player>();
	private ArrayList<Player> enemies = new ArrayList<Player>();
	
	// invokes the main native method
	private native void startClient(JNIBWAPI jniBWAPI);
	
	// query methods
	private native int getFrame();
	public native int getReplayFrameTotal();
	private native int[] getPlayersData();
	private native int[] getPlayerUpdate(int playerID);
	/** Returns string as a byte[] to properly handle ASCII-extended characters */
	private native byte[] getPlayerName(int playerID);
	private native int[] getResearchStatus(int playerID);
	private native int[] getUpgradeStatus(int playerID);
	private native int[] getAllUnitsData();
	private native int[] getRaceTypes();
	private native String getRaceTypeName(int unitTypeID);
	private native int[] getUnitTypes();
	private native String getUnitTypeName(int unitTypeID);
	private native int[] getRequiredUnits(int unitTypeID);
	private native int[] getTechTypes();
	private native String getTechTypeName(int techID);
	private native int[] getUpgradeTypes();
	private native String getUpgradeTypeName(int upgradeID);
	private native int[] getWeaponTypes();
	private native String getWeaponTypeName(int weaponID);
	private native int[] getUnitSizeTypes();
	private native String getUnitSizeTypeName(int sizeID);
	private native int[] getBulletTypes();
	private native String getBulletTypeName(int bulletID);
	private native int[] getDamageTypes();
	private native String getDamageTypeName(int damageID);
	private native int[] getExplosionTypes();
	private native String getExplosionTypeName(int explosionID);
	private native int[] getUnitCommandTypes();
	private native String getUnitCommandTypeName(int unitCommandID);
	private native int[] getOrderTypes();
	private native String getOrderTypeName(int unitCommandID);
	private native int[] getUnitIdsOnTile(int tx, int ty);
	
	// map data
	private native void analyzeTerrain();
	private native int getMapWidth();
	private native int getMapHeight();
	/** Returns string as a byte[] to properly handle ASCII-extended characters */
	private native byte[] getMapName();
	private native String getMapFileName();
	private native String getMapHash();
	private native int[] getHeightData();
	/** Returns the regionId for each map tile */
	private native int[] getRegionMap();
	private native int[] getWalkableData();
	private native int[] getBuildableData();
	private native int[] getChokePoints();
	private native int[] getRegions();
	private native int[] getPolygon(int regionID);
	private native int[] getBaseLocations();
	
	// unit commands: http://code.google.com/p/bwapi/wiki/Unit
	public native boolean attack(int unitID, int x, int y);
	public native boolean attack(int unitID, int targetID);
	public native boolean build(int unitID, int tx, int ty, int typeID);
	public native boolean buildAddon(int unitID, int typeID);
	public native boolean train(int unitID, int typeID);
	public native boolean morph(int unitID, int typeID);
	public native boolean research(int unitID, int techID);
	public native boolean upgrade(int unitID, int updateID);
	public native boolean setRallyPoint(int unitID, int x, int y);
	public native boolean setRallyPoint(int unitID, int targetID);
	public native boolean move(int unitID, int x, int y);
	public native boolean patrol(int unitID, int x, int y);
	public native boolean holdPosition(int unitID);
	public native boolean stop(int unitID);
	public native boolean follow(int unitID, int targetID);
	public native boolean gather(int unitID, int targetID);
	public native boolean returnCargo(int unitID);
	public native boolean repair(int unitID, int targetID);
	public native boolean burrow(int unitID);
	public native boolean unburrow(int unitID);
	public native boolean cloak(int unitID);
	public native boolean decloak(int unitID);
	public native boolean siege(int unitID);
	public native boolean unsiege(int unitID);
	public native boolean lift(int unitID);
	public native boolean land(int unitID, int tx, int ty);
	public native boolean load(int unitID, int targetID);
	public native boolean unload(int unitID, int targetID);
	public native boolean unloadAll(int unitID);
	public native boolean unloadAll(int unitID, int x, int y);
	public native boolean rightClick(int unitID, int x, int y);
	public native boolean rightClick(int unitID, int targetID);
	public native boolean haltConstruction(int unitID);
	public native boolean cancelConstruction(int unitID);
	public native boolean cancelAddon(int unitID);
	public native boolean cancelTrain(int unitID, int slot);
	public native boolean cancelMorph(int unitID);
	public native boolean cancelResearch(int unitID);
	public native boolean cancelUpgrade(int unitID);
	public native boolean useTech(int unitID, int typeID);
	public native boolean useTech(int unitID, int typeID, int x, int y);
	public native boolean useTech(int unitID, int typeID, int targetID);
	public native boolean placeCOP(int unitID, int tx, int ty);

	// utility commands
	public native void drawHealth(boolean enable);
	public native void drawTargets(boolean enable);
	public native void drawIDs(boolean enable);
	public native void enableUserInput();
	public native void enablePerfectInformation();
	public native void setGameSpeed(int speed);
	public native void setFrameSkip(int frameSkip);
	public native void leaveGame();
	
	// draw commands
	public native void drawBox(int left, int top, int right, int bottom, int color, boolean fill, boolean screenCoords);
	public native void drawCircle(int x, int y, int radius, int color, boolean fill, boolean screenCoords);
	public native void drawLine(int x1, int y1, int x2, int y2, int color, boolean screenCoords);
	public void drawLine(Point a, Point b, int color, boolean screenCoords) {
		drawLine(a.x, a.y, b.x, b.y, color, screenCoords);
	}
	public native void drawDot(int x, int y, int color, boolean screenCoords);
	public native void drawText(int x, int y, String msg, boolean screenCoords);
	public void drawText(Point a, String msg, boolean screenCoords) {
		drawText(a.x, a.y, msg, screenCoords);
	}
	
	// Extended Commands
	public native boolean isVisible(int tileX, int tileY);
	public native boolean isExplored(int tileX, int tileY);
	public native boolean isBuildable(int tx, int ty, boolean includeBuildings);
	public boolean isBuildable(int tx, int ty) { return isBuildable(tx, ty, false);}
	public native boolean hasCreep(int tileX, int tileY);
	public native boolean hasPower(int tileX, int tileY);
	public native boolean hasPower(int tileX, int tileY, int unitTypeID);
	public native boolean hasPower(int tileX, int tileY, int tileWidth, int tileHeight);
	public native boolean hasPower(int tileX, int tileY, int tileWidth, int tileHeight, int unitTypeID);
	public native boolean hasPowerPrecise(int x, int y);
	public native boolean hasPath(int fromX, int fromY, int toX, int toY);
	public native boolean hasPath(int unitID, int targetID);
	public native boolean hasPath(int unitID, int toX, int toY);
	public native boolean hasLoadedUnit(int unitID1, int unitID2);
	public native boolean canBuildHere(int tileX, int tileY, int unitTypeID, boolean checkExplored);
	public native boolean canBuildHere(int unitID, int tileX, int tileY, int unitTypeID, boolean checkExplored);
	public native boolean canMake(int unitTypeID);
	public native boolean canMake(int unitID, int unitTypeID);
	public native boolean canResearch(int techTypeID);
	public native boolean canResearch(int unitID, int techTypeID);
	public native boolean canUpgrade(int upgradeTypeID);
	public native boolean canUpgrade(int unitID, int upgradeTypeID);
	public native void printText(String message);
	public native void sendText(String message);
	public native void setCommandOptimizationLevel(int level);
	public native boolean isReplay();
	private native boolean isVisibleToPlayer(int unitID, int playerID);
	public boolean isVisibleToPlayer(Unit u, Player p) {
		return isVisibleToPlayer(u.getID(), p.getID());
	}
	public native int getLastError();
	public native int getRemainingLatencyFrames();

	// type data
	private HashMap<Integer, UnitType> unitTypes = new HashMap<Integer, UnitType>();
	private HashMap<Integer, RaceType> raceTypes = new HashMap<Integer, RaceType>();
	private HashMap<Integer, TechType> techTypes = new HashMap<Integer, TechType>();
	private HashMap<Integer, UpgradeType> upgradeTypes = new HashMap<Integer, UpgradeType>();
	private HashMap<Integer, WeaponType> weaponTypes = new HashMap<Integer, WeaponType>();
	private HashMap<Integer, UnitSizeType> unitSizeTypes = new HashMap<Integer, UnitSizeType>();
	private HashMap<Integer, BulletType> bulletTypes = new HashMap<Integer, BulletType>();
	private HashMap<Integer, DamageType> damageTypes = new HashMap<Integer, DamageType>();
	private HashMap<Integer, ExplosionType> explosionTypes = new HashMap<Integer, ExplosionType>();
	private HashMap<Integer, UnitCommandType> unitCommandTypes = new HashMap<Integer, UnitCommandType>();
	private HashMap<Integer, OrderType> orderTypes = new HashMap<Integer, OrderType>();
	private HashMap<Integer, EventType> eventTypes = new HashMap<Integer, EventType>();
	
	// type data accessors
	public UnitType getUnitType(int typeID) { return unitTypes.get(typeID); }
	public RaceType getRaceType(int typeID) { return raceTypes.get(typeID); }
	public TechType getTechType(int typeID) { return techTypes.get(typeID); }
	public UpgradeType getUpgradeType(int upgradeID) { return upgradeTypes.get(upgradeID); }
	public WeaponType getWeaponType(int weaponID) { return weaponTypes.get(weaponID); }
	public UnitSizeType getUnitSizeType(int sizeID) { return unitSizeTypes.get(sizeID); }
	public BulletType getBulletType(int bulletID) { return bulletTypes.get(bulletID); }
	public DamageType getDamageType(int damageID) { return damageTypes.get(damageID); }
	public ExplosionType getExplosionType(int explosionID) { return explosionTypes.get(explosionID); }
	public UnitCommandType getUnitCommandType(int unitCommandID) { return unitCommandTypes.get(unitCommandID); }
	public OrderType getOrderType(int orderID) { return orderTypes.get(orderID); }
	
	public Collection<UnitType> unitTypes() { return Collections.unmodifiableCollection(unitTypes.values()); }
	public Collection<RaceType> raceTypes() { return Collections.unmodifiableCollection(raceTypes.values()); }
	public Collection<TechType> techTypes() { return Collections.unmodifiableCollection(techTypes.values()); }
	public Collection<UpgradeType> upgradeTypes() { return Collections.unmodifiableCollection(upgradeTypes.values()); }
	public Collection<WeaponType> weaponTypes() { return Collections.unmodifiableCollection(weaponTypes.values()); }
	public Collection<UnitSizeType> unitSizeTypes() { return Collections.unmodifiableCollection(unitSizeTypes.values()); }
	public Collection<BulletType> bulletTypes() { return Collections.unmodifiableCollection(bulletTypes.values()); }
	public Collection<DamageType> damageTypes() { return Collections.unmodifiableCollection(damageTypes.values()); }
	public Collection<ExplosionType> explosionTypes() { return Collections.unmodifiableCollection(explosionTypes.values()); }
	public Collection<UnitCommandType> unitCommandTypes() { return Collections.unmodifiableCollection(unitCommandTypes.values()); }
	public Collection<OrderType> orderTypes() { return Collections.unmodifiableCollection(orderTypes.values()); }
	
	// game state accessors
	public int getFrameCount() { return gameFrame; }
	public Player getSelf() { return self; }
	public Player getNeutralPlayer() { return neutralPlayer; }
	public Player getPlayer(int playerID) { return players.get(playerID); }
	public Collection<Player> getPlayers() { return Collections.unmodifiableCollection(players.values()); }
	public List<Player> getAllies() { return Collections.unmodifiableList(allies); }
	public List<Player> getEnemies() { return Collections.unmodifiableList(enemies); }
	public Unit getUnit(int unitID) { return units.get(unitID); }
	public Collection<Unit> getAllUnits() { return Collections.unmodifiableCollection(units.values()); }
	public List<Unit> getMyUnits() { return Collections.unmodifiableList(playerUnits); }
	public List<Unit> getAlliedUnits() { return Collections.unmodifiableList(alliedUnits); }
	public List<Unit> getEnemyUnits() { return Collections.unmodifiableList(enemyUnits); }
	public List<Unit> getNeutralUnits() { return Collections.unmodifiableList(neutralUnits); }
	
	public List<Unit> getUnits(Player p) {
		List<Unit> pUnits = new ArrayList<Unit>();
		for (Unit u : units.values()) {
			if (u.getPlayerID() == p.getID()) {
				pUnits.add(u);
			}
		}
		return pUnits;
	}
	
	public List<Unit> getUnitsOnTile(int tx, int ty) {
		// Often will have 0 or few units on tile
		List<Unit> units = new ArrayList<Unit>(0);
		for (int id : getUnitIdsOnTile(tx, ty)) {
			units.add(getUnit(id));
		}
		return units;
	}
	
	/**
	 * Returns the map.
	 */
	public Map getMap() {
		return map;
	}
	
	/**
	 * Loads type data from BWAPI.
	 */
	private void loadTypeData() {
		// race types
		int[] raceTypeData = getRaceTypes();
		for (int index = 0; index < raceTypeData.length; index += RaceType.numAttributes) {
			RaceType type = new RaceType(raceTypeData, index);
			type.setName(getRaceTypeName(type.getID()));
			raceTypes.put(type.getID(), type);
		}
		
		// unit types
		int[] unitTypeData = getUnitTypes();
		for (int index = 0; index < unitTypeData.length; index += UnitType.numAttributes) {
			String name = getUnitTypeName(unitTypeData[index]);
			int[] requiredUnits = getRequiredUnits(unitTypeData[index]);
			UnitType type = new UnitType(unitTypeData, index, name, requiredUnits);
			unitTypes.put(type.getID(), type);
		}
		
		// tech types
		int[] techTypeData = getTechTypes();
		for (int index = 0; index < techTypeData.length; index += TechType.numAttributes) {
			TechType type = new TechType(techTypeData, index);
			type.setName(getTechTypeName(type.getID()));
			techTypes.put(type.getID(), type);
		}
		
		// upgrade types
		int[] upgradeTypeData = getUpgradeTypes();
		for (int index = 0; index < upgradeTypeData.length; index += UpgradeType.numAttributes) {
			UpgradeType type = new UpgradeType(upgradeTypeData, index);
			type.setName(getUpgradeTypeName(type.getID()));
			upgradeTypes.put(type.getID(), type);
		}
		
		// weapon types
		int[] weaponTypeData = getWeaponTypes();
		for (int index = 0; index < weaponTypeData.length; index += WeaponType.numAttributes) {
			WeaponType type = new WeaponType(weaponTypeData, index);
			type.setName(getWeaponTypeName(type.getID()));
			weaponTypes.put(type.getID(), type);
		}
		
		// unit size types
		int[] unitSizeTypeData = getUnitSizeTypes();
		for (int index = 0; index < unitSizeTypeData.length; index += UnitSizeType.numAttributes) {
			UnitSizeType type = new UnitSizeType(unitSizeTypeData, index);
			type.setName(getUnitSizeTypeName(type.getID()));
			unitSizeTypes.put(type.getID(), type);
		}
		
		// bullet types
		int[] bulletTypeData = getBulletTypes();
		for (int index = 0; index < bulletTypeData.length; index += BulletType.numAttributes) {
			BulletType type = new BulletType(bulletTypeData, index);
			type.setName(getBulletTypeName(type.getID()));
			bulletTypes.put(type.getID(), type);
		}
		
		// damage types
		int[] damageTypeData = getDamageTypes();
		for (int index = 0; index < damageTypeData.length; index += DamageType.numAttributes) {
			DamageType type = new DamageType(damageTypeData, index);
			type.setName(getDamageTypeName(type.getID()));
			damageTypes.put(type.getID(), type);
		}
		
		// explosion types
		int[] explosionTypeData = getExplosionTypes();
		for (int index = 0; index < explosionTypeData.length; index += ExplosionType.numAttributes) {
			ExplosionType type = new ExplosionType(explosionTypeData, index);
			type.setName(getExplosionTypeName(type.getID()));
			explosionTypes.put(type.getID(), type);
		}
		
		// unitCommand types
		int[] unitCommandTypeData = getUnitCommandTypes();
		for (int index = 0; index < unitCommandTypeData.length; index += UnitCommandType.numAttributes) {
			UnitCommandType type = new UnitCommandType(unitCommandTypeData, index);
			type.setName(getUnitCommandTypeName(type.getID()));
			unitCommandTypes.put(type.getID(), type);
		}
		
		// order types
		int[] orderTypeData = getOrderTypes();
		for (int index = 0; index < orderTypeData.length; index += OrderType.numAttributes) {
			OrderType type = new OrderType(orderTypeData, index);
			type.setName(getOrderTypeName(type.getID()));
			orderTypes.put(type.getID(), type);
		}
		
		// event types - no extra data to load
		for (EventType type : EventType.values()) {
			eventTypes.put(type.getID(), type);
		}
	}
	
	/**
	 * Loads map data and (if enableBWTA is true) BWTA data.
	 * 
	 * TODO: figure out how to use BWTA's internal map storage
	 */
	private void loadMapData() {
		String mapName = new String(getMapName(), charset);
		map = new Map(getMapWidth(), getMapHeight(), mapName, getMapFileName(), getMapHash(),
				getHeightData(), getBuildableData(), getWalkableData());
		if (!enableBWTA) {
			return;
		}
		
		// get region and choke point data
		File bwtaFile = new File(map.getHash() + ".jbwta");
		boolean analyzed = bwtaFile.exists();
		int[] regionMapData = null;
		int[] regionData = null;
		int[] chokePointData = null;
		int[] baseLocationData = null;
		HashMap<Integer, int[]> polygons = new HashMap<Integer, int[]>();
		
		// run BWTA
		if (!analyzed) {
			analyzeTerrain();
			regionMapData = getRegionMap();
			regionData = getRegions();
			chokePointData = getChokePoints();
			baseLocationData = getBaseLocations();
			for (int index = 0; index < regionData.length; index += Region.numAttributes) {
				int id = regionData[index];
				polygons.put(id, getPolygon(id));
			}
			
			// store the results to a local file (bwta directory)
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(bwtaFile));
				
				writeMapData(writer, regionMapData);
				writeMapData(writer, regionData);
				writeMapData(writer, chokePointData);
				writeMapData(writer, baseLocationData);
				for (int id : polygons.keySet()) {
					writer.write("" + id + ",");
					writeMapData(writer, polygons.get(id));
				}
				
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// load from file
		else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(bwtaFile));
				
				regionMapData = readMapData(reader);
				regionData = readMapData(reader);
				chokePointData = readMapData(reader);
				baseLocationData = readMapData(reader);
				// polygons (first integer is ID)
				int[] polygonData;
				while ((polygonData = readMapData(reader)) != null) {
					int[] coordinateData = Arrays.copyOfRange(polygonData, 1, polygonData.length);
					
					polygons.put(polygonData[0], coordinateData);
				}
				
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		map.initialize(regionMapData, regionData, polygons, chokePointData, baseLocationData);
	}
	
	/** Convenience method to write out each part of BWTA map data to a stream */
	private static void writeMapData(BufferedWriter writer, int[] data) throws IOException {
		boolean first = true;
		for (int val : data) {
			if (first) {
				first = false;
				writer.write("" + val);
			}
			else {
				writer.write("," + val);
			}
		}
		writer.write("\n");
	}
	
	/**
	 * Convenience method to read each part of BWTA map data from a stream
	 * 
	 * @return null when end of stream is reached, otherwise an int array (possibly empty)
	 */
	private static int[] readMapData(BufferedReader reader) throws IOException {
		int[] data = new int[0];
		String line = reader.readLine();
		if (line == null)
			return null;
		String[] stringData = line.split(",");
		if (stringData.length > 0 && !stringData[0].equals("")) {
			data = new int[stringData.length];
			for (int i = 0; i < stringData.length; i++) {
				data[i] = Integer.parseInt(stringData[i]);
			}
		}
		return data;
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Utility function for printing to the java console from C++.
	 */
	private void javaPrint(String msg) {
		try {
			System.out.println("Bridge: " + msg);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the client and event listener that a connection has been formed to the bridge.
	 */
	private void connected() {
		try {
			loadTypeData();
			listener.connected();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the client that a game has started. Not passed on to the event listener.<br>
	 * 
	 * Note: this is always called before the matchStarted event, and is meant as a way of notifying
	 * the AI client to clear up state.
	 */
	private void gameStarted() {
		try {
			// get the players
			self = null;
			allies.clear();
			allyIDs.clear();
			enemies.clear();
			enemyIDs.clear();
			players.clear();
			
			int[] playerData = getPlayersData();
			for (int index = 0; index < playerData.length; index += Player.numAttributes) {
				String name = new String(getPlayerName(playerData[index]), charset);
				Player player = new Player(playerData, index, name);
				
				players.put(player.getID(), player);
				
				if (player.isSelf()) {
					self = player;
				}
				else if (player.isAlly()) {
					allies.add(player);
					allyIDs.add(player.getID());
				}
				else if (player.isEnemy()) {
					enemies.add(player);
					enemyIDs.add(player.getID());
				}
				else if (player.isNeutral()) {
					neutralPlayer = player;
				}
			}
			
			// get unit data
			units.clear();
			playerUnits.clear();
			alliedUnits.clear();
			enemyUnits.clear();
			neutralUnits.clear();
			int[] unitData = getAllUnitsData();
			
			for (int index = 0; index < unitData.length; index += Unit.numAttributes) {
				int id = unitData[index];
				Unit unit = new Unit(id);
				unit.update(unitData, index);
				
				units.put(id, unit);
				if (self != null && unit.getPlayerID() == self.getID()) {
					playerUnits.add(unit);
				}
				else if (allyIDs.contains(unit.getPlayerID())) {
					alliedUnits.add(unit);
				}
				else if (enemyIDs.contains(unit.getPlayerID())) {
					enemyUnits.add(unit);
				}
				else {
					neutralUnits.add(unit);
				}
			}
			gameFrame = getFrame();
			loadMapData();
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the client that game data has been updated. Not passed on to the event listener.<br>
	 * 
	 * Note: this is always called before the events each frame, and is meant as a way of notifying
	 * the AI client to update state.
	 */
	private void gameUpdate() {
		try {
			// update game state
			gameFrame = getFrame();
			if (!isReplay()) {
				self.update(getPlayerUpdate(self.getID()));
				self.updateResearch(getResearchStatus(self.getID()), getUpgradeStatus(self.getID()));
			} else {
				for (Integer playerID : players.keySet()) {
					players.get(playerID).update(getPlayerUpdate(playerID));
					players.get(playerID).updateResearch(getResearchStatus(playerID),
							getUpgradeStatus(playerID));
				}
			}
			// update units
			int[] unitData = getAllUnitsData();
			HashSet<Integer> deadUnits = new HashSet<Integer>(units.keySet());
			ArrayList<Unit> playerList = new ArrayList<Unit>();
			ArrayList<Unit> alliedList = new ArrayList<Unit>();
			ArrayList<Unit> enemyList = new ArrayList<Unit>();
			ArrayList<Unit> neutralList = new ArrayList<Unit>();
			
			for (int index = 0; index < unitData.length; index += Unit.numAttributes) {
				int id = unitData[index];
				
				// bugfix - unit list was emptying itself every second frame
				deadUnits.remove(id);
				
				Unit unit = units.get(id);
				if (unit == null) {
					unit = new Unit(id);
					units.put(id, unit);
				}
				
				unit.update(unitData, index);
				
				if (self != null)
				{
					if (unit.getPlayerID() == self.getID()) {
						playerList.add(unit);
					}
					else if (allyIDs.contains(unit.getPlayerID())) {
						alliedList.add(unit);
					}
					else if (enemyIDs.contains(unit.getPlayerID())) {
						enemyList.add(unit);
					}
					else {
						neutralList.add(unit);
					}
				}
				else if (allyIDs.contains(unit.getPlayerID())) {
					alliedList.add(unit);
				}
				else if (enemyIDs.contains(unit.getPlayerID())) {
					enemyList.add(unit);
				}
				else {
					neutralList.add(unit);
				}
			}
			
			// update the unit lists
			playerUnits = playerList;
			alliedUnits = alliedList;
			enemyUnits = enemyList;
			neutralUnits = neutralList;
			for (Integer unitID : deadUnits) {
				units.get(unitID).setDestroyed();
				units.remove(unitID);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the event listener that the game has terminated.<br>
	 * 
	 * Note: this is always called after the matchEnded event, and is meant as a way of notifying
	 * the AI client to clear up state.
	 */
	private void gameEnded() {}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Sends BWAPI callback events to the event listener.
	 */
	private void eventOccurred(int eventTypeID, int param1, int param2, String param3) {
		try {
			EventType event = eventTypes.get(eventTypeID);
			switch (event) {
				case MatchStart:
					listener.matchStart();
					break;
				case MatchEnd:
					listener.matchEnd(param1 == 1);
					break;
				case MatchFrame:
					
					if(count % 6 == 0)
						listener.matchFrame();
					
					count++;
					
					break;
				case MenuFrame:
					// Unused?
					break;
				case SendText:
					listener.sendText(param3);
					System.out.println("Send Text :" + param3);
					break;
				case ReceiveText:
					listener.receiveText(param3);
					System.out.println("Receive Text :" + param3);
					break;
				case PlayerLeft:
					listener.playerLeft(param1);
					break;
				case NukeDetect:
					if (param1 == -1)
						listener.nukeDetect();
					else
						listener.nukeDetect(param1, param2);
					break;
				case UnitDiscover:
					listener.unitDiscover(param1);
					break;
				case UnitEvade:
					listener.unitEvade(param1);
					break;
				case UnitShow:
					listener.unitShow(param1);
					break;
				case UnitHide:
					listener.unitHide(param1);
					break;
				case UnitCreate:
					listener.unitCreate(param1);
					break;
				case UnitDestroy:
					listener.unitDestroy(param1);
					break;
				case UnitMorph:
					listener.unitMorph(param1);
					break;
				case UnitRenegade:
					listener.unitRenegade(param1);
					break;
				case SaveGame:
					listener.saveGame(param3);
					break;
				case UnitComplete:
					listener.unitComplete(param1);
					break;
				case PlayerDropped:
					listener.playerDropped(param1);
					System.out.println("Drop Player :" + param1);
					break;
				case None:
					// Unused?
					break;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the event listener that a key was pressed.
	 */
	public void keyPressed(int keyCode) {
		try {
			listener.keyPressed(keyCode);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
