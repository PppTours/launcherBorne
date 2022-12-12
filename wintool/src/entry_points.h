#pragma once

#include <string>

namespace substitutions {

void start();

}

namespace key_logger {

void start();

}

namespace send_stroke {

void start(const std::string &arg);

}

namespace entry_points {

inline void substitutions() { substitutions::start(); }
inline void key_logger() { key_logger::start(); }
inline void send_stroke(const std::string &arg) { send_stroke::start(arg); }

}