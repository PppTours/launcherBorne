#pragma once

namespace substitutions {

void start();

}

namespace key_logger {

void start();

}

namespace simple_click {

void start();

}

namespace entry_points {

inline void substitutions() { substitutions::start(); }
inline void key_logger() { key_logger::start(); }
inline void simple_click() { simple_click::start(); }

}