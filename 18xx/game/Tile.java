/* $Header: /Users/blentz/rails_rcs/cvs/18xx/game/Attic/Tile.java,v 1.2 2005/10/30 16:29:56 evos Exp $
 * 
 * Created on 23-Oct-2005
 * Change Log:
 */
package game;

import java.util.*;
import java.util.regex.*;

import org.w3c.dom.*;
import util.XmlUtils;

/**
 * @author Erik Vos
 */
public class Tile implements TileI {
    
    int id;
    String name;
    String colour; // May become a separate class TileType
    boolean upgradeable;
    List upgrades = new ArrayList();
    String upgradesString = "";
    List[] tracksPerSide = new ArrayList[6];
    List tracks = new ArrayList();
    List stations = new ArrayList();
    
    public Tile (Integer id) {
        this.id = id.intValue();
        
        for (int i=0; i<6; i++) tracksPerSide[i] = new ArrayList();
            
    }

    /**
     * @param se &lt;Tile&gt; element from TileSet.xml
     * @param te &lt;Tile&gt; element from Tiles.xml 
     */
    public void configureFromXML (Element se, Element te) throws ConfigurationException {
        
        /* EV 23oct05: There is a lot to read and configure here,
         * for now we only read the tracks to determine
         * the impassable hexsides of offmap and fixed preprinted track.
         */
        
        NamedNodeMap teAttr = te.getAttributes();
        colour = XmlUtils.extractStringAttribute(teAttr, "colour");
        if (colour == null) throw new ConfigurationException("Missing colour in tile "+id);

        upgradeable = !colour.equals("red") && !colour.equals("fixed");
        
        /* Tracks (only number per side, no cities yet) */
        NodeList trackElements = te.getElementsByTagName("Track");
        Element trackElement;
        Track track;
        int from, to;
        String fromStr, toStr;
        NamedNodeMap nnp;
        for (int i=0; i<trackElements.getLength(); i++) {
            trackElement = (Element) trackElements.item(i);
            nnp = trackElement.getAttributes();
            fromStr = XmlUtils.extractStringAttribute(nnp, "from");
            toStr = XmlUtils.extractStringAttribute(nnp, "to");
            if (fromStr == null || toStr == null) {
                throw new ConfigurationException ("Missing from or to in tile "+id);
            }

            from = getPointNumber (fromStr);
            to = getPointNumber (toStr);
            track = new Track (from, to);
            tracks.add (track);
            if (from >= 0) tracksPerSide[from].add(track);
            if (to >= 0) tracksPerSide[to].add(track);
        }
        
        /* Stations */
        NodeList stationElements = te.getElementsByTagName("Station");
        Element stationElement;
        String sid, type;
        int value, slots;
        Station station;
        for (int i=0; i<stationElements.getLength(); i++) {
            stationElement = (Element) stationElements.item(i);
            nnp = stationElement.getAttributes();
            sid = XmlUtils.extractStringAttribute(nnp, "id");
            if (sid == null) throw new ConfigurationException ("Tile "+this.id+" has Station w/o id");
            type = XmlUtils.extractStringAttribute(nnp, "type");
            if (type == null) throw new ConfigurationException ("Tile "+this.id+" has Station w/o type");
            value = XmlUtils.extractIntegerAttribute(nnp, "value", 0);
            slots = XmlUtils.extractIntegerAttribute(nnp, "slots", 0);
            station = new Station (sid, type, value, slots);
            stations.add(station);
            //System.out.println("Tile "+this.id+" station "+type);
        }
        
        /* Upgrades */
        NodeList upgnl = se.getElementsByTagName("Upgrade");
        String ids;
        int id;
        String[] idArray; 
        TileI upgrade;
        for (int i=0; i<upgnl.getLength(); i++) {
            trackElement = (Element) trackElements.item(i);
            nnp = ((Element)upgnl.item(i)).getAttributes();
            ids = XmlUtils.extractStringAttribute(nnp, "id");
            upgradesString = ids; // TEMPORARY
            //System.out.println("Tile "+name+" upgrades "+ids);
            if (ids != null) {
                idArray = ids.split(",");
                for (int j=0; j<idArray.length; j++) {
                    try {
                        id = Integer.parseInt(idArray[j]);
                        upgrade = TileManager.get().getTile(id);
                        if (upgrade != null) {
                            upgrades.add (upgrade);
                        } else {
                            throw new ConfigurationException ("Tile "+name+": upgrade "+id+" not found");
                        }
                    } catch (NumberFormatException e) {
                        throw new ConfigurationException ("Tile "+name+": non-numeric upgrade "+idArray[j], e);
                    }
                    
                }
            	
            }
        }
         
        
    }
    
    
    
    /**
     * @return Returns the colour.
     */
    public String getColour() {
        return colour;
    }
    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    private static final Pattern sidePattern = Pattern.compile("side(\\d+)");
    private static final Pattern cityPattern = Pattern.compile("city(\\d+)");
    
    private int getPointNumber (String trackEnd) throws ConfigurationException {
        
        Matcher m;
        if ((m = sidePattern.matcher(trackEnd)).matches()) {
            return Integer.parseInt(m.group(1))%6;
        } else if ((m = cityPattern.matcher(trackEnd)).matches()) {
            return -Integer.parseInt(m.group(1));
        }
        // Should add some validation!
        throw new ConfigurationException ("Invalid track end: "+trackEnd);
    }
    
    public boolean hasTracks(int sideNumber) {
        return (tracksPerSide[sideNumber%6].size() > 0);
    }
    
    public boolean isUpgradeable () {
        return upgradeable;
    }
    
    /**
     * Get the valid upgrades if this tile on a certain hex
     * (restrictions per hex have noit yer been implemented).
     * @param hex The MapHex to be upgraded.
     * @return A List of valid upgrade TileI objects.
     */
    public List getUpgrades (MapHex hex) {
        return upgrades;
    }
    
    public String getUpgradesString (MapHex hex) {
        /*
        Iterator it = getUpgrades (hex).iterator();
        StringBuffer b = new StringBuffer();
        while (it.hasNext()) {
            if (b.length() > 0) b.append(",");
            b.append (((TileI)it.next()).getName());
        }
        return b.toString();
        */
        return upgradesString;
    }
    
    public boolean hasStations () {
        return stations.size() > 0;
    }
    
    public List getStations() {
        return stations;
    }

}
