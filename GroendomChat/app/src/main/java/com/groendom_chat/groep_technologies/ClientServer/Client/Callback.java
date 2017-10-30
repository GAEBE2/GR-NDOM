package com.groendom_chat.groep_technologies.ClientServer.Client;

import java.io.Serializable;

/**
 * Created by Patrick Wissiak on 22.10.2017.
 */

public interface Callback extends Serializable {

  void onFinish(Object... param);
}
