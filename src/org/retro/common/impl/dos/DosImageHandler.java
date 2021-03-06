/*
 * (C) Copyright ${year} retro-io (https://github.com/marcelschoen/retro-io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.retro.common.impl.dos;

import de.waldheinz.fs.FsDirectory;
import de.waldheinz.fs.FsDirectoryEntry;
import de.waldheinz.fs.FsFile;
import de.waldheinz.fs.fat.FatFileSystem;
import de.waldheinz.fs.fat.FatLfnDirectoryEntry;
import de.waldheinz.fs.util.FileDisk;
import org.retro.common.VirtualDirectory;
import org.retro.common.VirtualDisk;
import org.retro.common.VirtualDiskException;
import org.retro.common.VirtualFile;
import org.retro.common.impl.AbstractBaseImageHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Reads and writes DOS RAW images.
 *
 * Uses the fat32-lib library by Matthias Treydte:
 * https://github.com/waldheinz/fat32-lib
 *
 * @author Marcel Schoen
 */
public class DosImageHandler extends AbstractBaseImageHandler
{

	@Override
	public VirtualDisk loadImage(File imageFile) throws VirtualDiskException
	{
		FatFileSystem fs = null;
		FileDisk fileDisk = null;
		try
		{
			fileDisk = new FileDisk(imageFile, false);
			fs = FatFileSystem.read(fileDisk, true);
		}
		catch (IOException e)
		{
			throw new VirtualDiskException(
					"Failed to read DOS image; reason: " + e.toString() + ", image: " + imageFile.getAbsolutePath(), e);
		}

		DosVirtualDisk virtualDisk = new DosVirtualDisk(fs, imageFile.getName());
		try
		{
			FsDirectory rootDirectory = fs.getRoot();
			VirtualDirectory root = new VirtualDirectory("/");

			iterateReadDirectory(root, rootDirectory);

			virtualDisk.setRootContent(root);

		}
		catch (IOException e)
		{
			throw new VirtualDiskException("Failed to read DOS files from image; reason: " + e.toString(), e);
		}

		return virtualDisk;
	}

	private void iterateReadDirectory(VirtualDirectory parent, FsDirectory directory) throws IOException
	{
		Iterator<FsDirectoryEntry> iterator = directory.iterator();
		while (iterator.hasNext())
		{
			FsDirectoryEntry entry = iterator.next();
			if (entry.isFile())
			{
				try
				{
					VirtualFile virtualFile = new VirtualFile(parent, entry.getName());
					FsFile fsFile = entry.getFile();
					ByteBuffer bb = ByteBuffer.allocate((int) fsFile.getLength());
					fsFile.read(0, bb);
					virtualFile.setContent(bb);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				if (".".equalsIgnoreCase(entry.getName()))
					return;
				VirtualDirectory newParent = new VirtualDirectory(parent, entry.getName());
				iterateReadDirectory(newParent, entry.getDirectory());
			}
		}
	}

	@Override
	public void writeImage(VirtualDisk floppyDisk, File imageFile) throws VirtualDiskException
	{
		try
		{
			// throw new VirtualDiskException("*not implemented yet*");

			FileDisk fileDisk = new FileDisk(imageFile, false);
			FatFileSystem fs = FatFileSystem.read(fileDisk, false);
			FatLfnDirectoryEntry fatEntry = fs.getRoot().addFile(imageFile.getName());

			fs.flush();
			fileDisk.flush();

		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new VirtualDiskException("*not implemented yet*");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new VirtualDiskException("*not implemented yet*");
		}
	}
}
