file(GLOB JSON_SOURCES *.cpp)

add_library(json SHARED ${JSON_SOURCES})
GENERATE_EXPORT_HEADER(json)
target_include_directories(json PUBLIC ${CMAKE_CURRENT_BINARY_DIR})
install(TARGETS json LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR})