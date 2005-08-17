/*
 * Created on 05-Mar-2005
 *
 * IG Adams
 */
package game;

import java.util.List;

/**
 * @author iadams
 *
 * Interface for CompanyManager objects. A company manager is a factory
 * which vends Company objects.
 */
public interface TrainManagerI {

    /** This is the name by which the TrainManager should be registered with the ComponentManager. */
    static final String COMPONENT_NAME = "TrainManager";

}
