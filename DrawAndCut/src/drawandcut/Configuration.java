package drawandcut;

/**
 *
 * @author akouznet
 */
public class Configuration {
    
    public static final double MATERIAL_SIZE_X = 210; // mm
    public static final double MATERIAL_SIZE_Y = 210; // mm
    public static final double MATERIAL_SIZE_RATIO = MATERIAL_SIZE_X / MATERIAL_SIZE_Y;
    
    public static final double IN = 25.4; // mm
    
    public static final double TOOL_DIAMETER = 1/8. * IN; // mm
    
    public static final double LINE_WIDTH = 4 * TOOL_DIAMETER; // mm
    
}
