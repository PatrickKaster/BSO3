package net.cropsense.bso3;

import de.grogra.pf.registry.Plugin;

/**
 * Plugin for the application of Boolean Set Operations 3 to the geometric
 * primitives of GroIMP. Large parts of the code are based on the Realtime-CSG
 * library by Sander van Rossen / Matthew Baranowski.
 * The original code can be downloaded at
 * https://github.com/LogicalError/Realtime-CSG/
 * 
 * @author Florian Schoeler, Patrick Kaster
 */
public class BSO3Plugin extends Plugin
{

	public boolean initialize()
	{
		return true;
	}
}
