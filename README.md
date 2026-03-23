
# ROS2Craft: Advanced Robotics in Minecraft!

This dev branch uses [rctoris:jrosbridge](https://github.com/rctoris/jrosbridge) as Java-ROS2 interface, based on the rosbridge_suite using websockets.

Obtained as local .jar lib using:
```
dependencies {
  implementation files("libs/jrosbridge-0.2.2.jar")
}
```
Loaded locally becuase original version was not performing topic unadvertise call properly. The difference is as follows;
```
public void unadvertise() {
    // build and send the rosbridge call
    // String unadvertiseId = "unadvertise:" + this.name + ":"
    // + this.ros.nextId();
    JsonObject call = Json.createObjectBuilder()
            .add(JRosbridge.FIELD_OP, JRosbridge.OP_CODE_UNADVERTISE)
            // .add(JRosbridge.FIELD_ID, unadvertiseId)
            .add(JRosbridge.FIELD_TOPIC, this.name).build();
    this.ros.send(call);

    // set the flag indicating we are no longer registered
    this.isAdvertised = false;
	}
```

The call had a different id, by removing it should be enough to unadvertise as expected.