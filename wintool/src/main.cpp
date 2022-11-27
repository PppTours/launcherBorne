#include <iostream>
#include <string>

#include "entry_points.h"

int main(int argc, const char **argv)
{
  argc--; argv++; // skip program name

  if (argc != 1) {
    std::cerr << "Missing action: substitution|key_logger|simple_click" << std::endl;
    return 1;
  }

  std::string action = argv[0];
  if (action == "substitution") entry_points::substitutions();
  else if (action == "key_logger") entry_points::key_logger();
  else if (action == "simple_click") entry_points::simple_click();
  else std::cerr << "Unknown action '" << action << "'" << std::endl;

  return 0;
}