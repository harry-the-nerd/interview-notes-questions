.PHONY: all
all:
	# Copy all the JSON files in the current directory to every subfolder (excludes demo.json)
	@for dir in */; do \
		cp *.json $$dir; \
		rm -f $$dir/demo.json; \
	done

