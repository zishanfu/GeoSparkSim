/**
 * FILE: GraphHooperConf Copyright (c) 2015 - 2020 Data Systems Lab at Arizona State University
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zishanfu.geosparksim.shortestpath;

import com.graphhopper.util.CmdArgs;
import java.util.HashMap;
import java.util.Map;

public class GraphHooperConf {
    public static CmdArgs produceCmgArgs(String osmFilePath) {
        Map<String, String> kvArgs = new HashMap<String, String>();
        kvArgs.put("graph.dataaccess".toLowerCase(), "RAM_STORE");
        kvArgs.put("prepare.ch.weightings".toLowerCase(), "no");
        kvArgs.put("prepare.minNetworkSize".toLowerCase(), "200");
        kvArgs.put("prepare.minOneWayNetworkSize".toLowerCase(), "200");
        kvArgs.put("datareader.file".toLowerCase(), osmFilePath);
        CmdArgs cmdArgs = new CmdArgs(kvArgs);
        return cmdArgs;
    }
}
