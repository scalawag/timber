// timber -- Copyright 2012-2015 -- Justin Patterson
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package bridge;

import org.slf4j.*;

public class JavaMain {
    public static void main(String[] args) {
        // If you remove this call to Logging.configurate(), the slf4j bridge will use the default dispatcher in the
        // system.  With this call, you can set up the slf4j bridge to use a different dispatcher.
        Logging.configurate();
        Logger log = LoggerFactory.getLogger("ABC");
        log.debug("Hello, world!");
        log.debug("Goodbye, world!",new IllegalArgumentException("psych"));
        log.debug("{}, {}!","Hello again","world");
    }
}

